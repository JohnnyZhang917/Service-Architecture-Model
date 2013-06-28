package eu.pmsoft.sam.see


import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable
import com.google.inject._
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model._
import eu.pmsoft.sam.model.ServiceInstanceID
import eu.pmsoft.sam.model.SamServiceImplementation
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.SamService
import eu.pmsoft.sam.model.SamServiceInstance
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalBindingController, ExtrenalBindingInfrastructureModule}
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract
import eu.pmsoft.sam.execution.ServiceAction
import eu.pmsoft.sam.idgenerator.{LongLongIdGenerator, LongLongID}

object ServiceExecutionEnvironment {

  def apply(configuration: SEEConfiguration) = new ServiceExecutionEnvironment(configuration)

  def configurationBuilder(port: Int) = SEEConfigurationBuilder(SEEConfiguration(Set(), Set(), port))
}

class ServiceExecutionEnvironment(val configuration: SEEConfiguration) {

  private val status = new ServiceExecutionEnvironmentStatus(configuration.implementations, configuration.architectures)
  private val transport = new SamInjectionTransaction(status, configuration.port)

  val architectureManager: SamArchitectureManagementApi = new SamArchitectureManagement(status)
  val serviceRegistry: SamServiceRegistryApi = new SamServiceRegistry(status)
  val executionNode: SamExecutionNodeApi = new SamExecutionNode(serviceRegistry, architectureManager, status)

  val transactionApi: SamInjectionTransactionApi = transport

  lazy val javaApiModule: Module = new AbstractModule() {
    def configure() {
      bind(classOf[SamArchitectureManagementApi]).toInstance(architectureManager)
      bind(classOf[SamServiceRegistryApi]).toInstance(serviceRegistry)
      bind(classOf[SamExecutionNodeApi]).toInstance(executionNode)
      bind(classOf[SamInjectionTransactionApi]).toInstance(transactionApi)
    }
  }

}

private class SamArchitectureManagement(val status: ServiceExecutionEnvironmentStatus) extends SamArchitectureManagementApi {

  def getService(key: SamServiceKey) = status.serviceByKey(key)
}

private class SamServiceRegistry(val status: ServiceExecutionEnvironmentStatus) extends SamServiceRegistryApi {

  def getImplementation(key: SamServiceImplementationKey): SamServiceImplementation = {
    status.implementationRegistry.getOrElse(key, null)
  }

}

private class SamExecutionNode(val serviceRegistry: SamServiceRegistryApi,
                               val architectureManager: SamArchitectureManagementApi,
                               val status: ServiceExecutionEnvironmentStatus) extends SamExecutionNodeApi {

  val logger = LoggerFactory.getLogger(this.getClass)

  def createServiceInstance(key: SamServiceImplementationKey): SamServiceInstance = {
    logger.trace("createServiceInstance {}", key)
    val implementation = serviceRegistry.getImplementation(key)
    val instanceInjector: Injector = createServiceInjector(implementation)
    val instance = SamServiceInstance(ServiceInstanceID(), implementation, instanceInjector)
    status.addServiceInstance(instance)
    instance
  }

  private def createServiceInjector(implementation: SamServiceImplementation): Injector = {
    logger.trace("createServiceInjector {}", implementation)
    val moduleInstance = implementation.implKey.module.newInstance()
    val serviceContracts: Seq[Set[Key[_]]] = implementation.bindServices.map(architectureManager.getService _).map(_.api)
    Guice.createInjector(moduleInstance, FreeBindingBuilder.buildFreeBindingModule(serviceContracts))
  }

  def getServiceInstances(key: SamServiceImplementationKey): Set[ServiceInstanceID] = {
    status.getRunningServiceInstances.filter(_._2.implementation.implKey == key).map(_._1).toSet
  }

  def getInstance(id: ServiceInstanceID): SamServiceInstance = status.getRunningServiceInstances.getOrElse(id, null)

  def registerInjectionConfiguration(element: InjectionConfigurationElement): ServiceConfigurationID = {
    logger.trace("registerInjectionConfiguration {}", element.contract)
    val configuration = InjectionConfiguration(element)
    status.addInjectionConfiguration(configuration)
    configuration.configurationId
  }

}


private object FreeBindingBuilder {

  def buildFreeBindingModule(contracts: Seq[Set[Key[_]]]): Module = {
    new PrivateModule {
      def configure() {
        val b = binder()
        b.requireExplicitBindings()
        contracts.zipWithIndex.foreach {
          case (service, slotNr) => service.foreach {
            key => FreeBindingInjectionUtil.createIntermediateProvider(b, key, slotNr)
          }
        }
        install(new ExtrenalBindingInfrastructureModule())
        expose(classOf[ExternalBindingController])
      }
    }
  }
}

private class ServiceExecutionEnvironmentStatus(val implementations: Set[SamServiceImplementationPackageContract],
                                                val architectures: Set[SamArchitectureDefinition]) {
  private val instanceRunning: mutable.Map[ServiceInstanceID, SamServiceInstance] = mutable.Map.empty
  private val configurations: mutable.Map[ServiceConfigurationID, InjectionConfiguration] = mutable.Map.empty
  private val exposedServicesUrl: mutable.Map[ServiceConfigurationID, ServiceInstanceURL] = mutable.Map.empty
  private val exposedServiceConfigurations: mutable.Map[ServiceInstanceURL, ServiceConfigurationID] = mutable.Map.empty
  val logger = LoggerFactory.getLogger(this.getClass)

  // Registration of implementations
  val implementationRegistry: Map[SamServiceImplementationKey, SamServiceImplementation] = Map() ++ {
    implementations map {
      SamModelBuilder.loadImplementationPackage _
    } flatMap {
      _ map (s => s.implKey -> s)
    }
  }

  val serviceByKey: Map[SamServiceKey, SamService] = Map() ++ {
    architectures map {
      SamModelBuilder.loadArchitectureDefinition _
    } flatMap {
      _.categories.flatMap(c => c.services).map(s => s.id -> s)
    }
  }


  def addServiceInstance(instance: SamServiceInstance) = instanceRunning.update(instance.instanceId, instance)

  def addInjectionConfiguration(configuration: InjectionConfiguration) = configurations.update(configuration.configurationId, configuration)

  def exposeServiceConfiguration(configurationId: ServiceConfigurationID, urlCreator: ServiceConfigurationID => ServiceInstanceURL): ServiceInstanceURL = {
    val exposedUrl = exposedServicesUrl.get(configurationId) match {
      case Some(url) => url
      case None => {
        val url = urlCreator(configurationId)
        exposedServicesUrl.put(configurationId, url)
        exposedServiceConfigurations.put(url, configurationId)
        url
      }
    }
    logger.trace("exposeServiceConfiguration {}, url: {}", configurationId, exposedUrl: Any)
    exposedUrl
  }

  def getRunningServiceInstances = instanceRunning

  def getConfigurations = configurations

  def getExposedServicesUrl = exposedServicesUrl

  def getExposedServiceConfigurations = exposedServiceConfigurations

}

private class SamInjectionTransaction(
                                       val status: ServiceExecutionEnvironmentStatus,
                                       val port: Int
                                       ) extends SamInjectionTransactionApi {

  val logger = LoggerFactory.getLogger(this.getClass)
  val server: CanonicalTransportServer = new CanonicalTransportServer(this:SamInjectionTransactionApi, port)
  val transactionIdGenerator = LongLongIdGenerator.createGenerator()

  private def createExecutionContext(tid: ServiceConfigurationID): InjectionTransactionContext = {
    val config = status.getConfigurations(tid)
    val contextBuilder = CanonicalRecordingLayer(createExternalServiceConnections _, createLoopBackEndpoint _)
    val context = contextBuilder.createContext(config.configurationRoot)
    logger.trace("created context for configuration {}", config.configurationRoot)
    context
  }

  private def createLoopBackEndpoint(): LoopBackExecutionPipe = {
    new TMPSlotExecutionPipe()
  }

  def createUrl(configurationId: ServiceConfigurationID): ServiceInstanceURL = {
    logger.trace("createUrl {}", configurationId)
    ServiceInstanceURL(new java.net.URL("http", server.serverAddress.getHostName, server.serverAddress.getPort, s"/service/${configurationId.id}"))
  }

  def createExternalServiceConnections(endpoints: Seq[ExternalServiceBind]): Seq[SlotExecutionPipe] = {
    val localConfig = status.getExposedServiceConfigurations
    logger.debug("bind service {}", endpoints)
    val pipes = endpoints.map {
      case ExternalServiceBind(_, url) if localConfig.contains(url) => {
        val context = createExecutionContext(localConfig(url))
        new DirectExecutionPipe(context)
      }
      case ExternalServiceBind(_, url) => server.getExecutionPipe(url)
    }
    pipes
  }

  def liftServiceConfiguration(configurationId: ServiceConfigurationID): ServiceInstanceURL = status.exposeServiceConfiguration(configurationId, createUrl)

  def getTransaction(tid: ServiceConfigurationID): InjectionTransactionAccessApi = {
    createExecutionContext(tid)
  }

  def executeServiceAction[R, T](configurationId: ServiceConfigurationID, action: ServiceAction[R, T]): Future[R] = {
    val injectionExecutionContext = createExecutionContext(configurationId)
    ThreadExecutionModel.openTransactionThread(injectionExecutionContext).executeServiceAction(action)
  }

  def openTransactionContext(url: ServiceInstanceURL): Future[LongLongID] = Future {
    val tid = transactionIdGenerator.getNextID()
    ???
    tid
  }


}
