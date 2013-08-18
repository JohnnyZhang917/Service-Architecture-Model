package eu.pmsoft.sam.see


import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable
import com.google.inject._
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model._
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalBindingController, ExtrenalBindingInfrastructureModule}
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract
import eu.pmsoft.sam.execution.ServiceAction
import eu.pmsoft.sam.idgenerator.{LongLongIdGenerator, LongLongID}
import scala.Some
import java.net.URL
import eu.pmsoft.sam.model.InjectionConfiguration
import eu.pmsoft.sam.model.SamService
import scala.Some
import eu.pmsoft.sam.model.ExposedServiceTransaction
import eu.pmsoft.sam.model.SamServiceImplementation
import eu.pmsoft.sam.model.ServiceInstanceURL
import eu.pmsoft.sam.model.LiftedServiceConfiguration
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.SamServiceInstance
import eu.pmsoft.sam.model.ExternalServiceBind
import eu.pmsoft.sam.model.SEEConfigurationBuilder
import eu.pmsoft.sam.model.SEEConfiguration
import eu.pmsoft.sam.model.SamServiceImplementationKey
import eu.pmsoft.sam.definition.service.SamServiceDefinition

object ServiceExecutionEnvironment {

  def apply(configuration: SEEConfiguration) = new ServiceExecutionEnvironment(configuration)

  def configurationBuilder(port: Int) = SEEConfigurationBuilder(SEEConfiguration(Set(), Set(), port))
}

class ServiceExecutionEnvironment(val configuration: SEEConfiguration) extends EnvironmentExternalApiLogic {

  private[this] val status = new ServiceExecutionEnvironmentStatus(configuration.implementations, configuration.architectures)
  private[this] val server = new CanonicalTransportServer(this, configuration.port)

  // Visible for testing
  val architectureManager: SamArchitectureManagementApi = new SamArchitectureManagement(status)
  private[see] val transaction = new SamInjectionTransaction(architectureManager, status, server.createUrl _)
  val serviceRegistry: SamServiceRegistryApi = new SamServiceRegistry(status)
  val executionNode: SamExecutionNodeApi = new SamExecutionNode(serviceRegistry, architectureManager, status)
  val transactionApi: SamInjectionTransactionApi = transaction

  lazy val clientApiModule: Module = new AbstractModule() {
    def configure() {
      bind(classOf[SamArchitectureManagementApi]).toInstance(architectureManager)
      bind(classOf[SamServiceRegistryApi]).toInstance(serviceRegistry)
      bind(classOf[SamExecutionNodeApi]).toInstance(executionNode)
      bind(classOf[SamInjectionTransactionApi]).toInstance(transactionApi)
      bind(classOf[SamEnvironmentExternalConnector]).toInstance(server)
    }
  }

  def getArchitectureInfo(): String = status.architectures map { arch => arch.signature } mkString(":")

  def getExposedServiceTransaction(): Seq[ExposedServiceTransaction] = status.getExposedServiceConfigurations.values map { liftId =>
    ExposedServiceTransaction(liftId.url, status.getConfigurations(liftId.configId).configurationRoot.contractKey)
  } toSeq
}

trait EnvironmentExternalApiLogic {

  def getArchitectureInfo(): String

  def getExposedServiceTransaction() : Seq[ExposedServiceTransaction]

  private def mapExposedTo(ref : ExposedServiceTransaction) : SamExposedServiceReference = SamExposedServiceReference(ref.url.url.toExternalForm, ref.contract.signature.getCanonicalName)

  def handle(action: SamEnvironmentAction): Future[SamEnvironmentResult] = {
    import SamEnvironmentCommandType._
    Future {
      val result = SamEnvironmentResult.getDefaultInstance.copy(`command` = action.`command`)
      action.`command` match {
        case ARCHITECTURE_INFO => result.setArchitectureInfoSignature(getArchitectureInfo())
        case PING => result
        case SERVICE_LIST => result.addAllServiceList(getExposedServiceTransaction() map mapExposedTo _)
        case _ => ???
      }
    }
  }

  // FIXME: security check and validation

  def createUrl(urlStr : String) : ServiceInstanceURL = ServiceInstanceURL(new URL(urlStr))

  def findServiceContract(contractStr : String) : SamServiceKey= {
    val loadClass: Class[_] = this.getClass.getClassLoader.loadClass(contractStr)
    SamServiceKey(loadClass.asSubclass(classOf[SamServiceDefinition]))
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
    logger.trace("registerInjectionConfiguration {}", element.contractKey)
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

private class ServiceExecutionEnvironmentStatus(val implementationContracts: Set[SamServiceImplementationPackageContract],
                                                val architectureDefinitions: Set[SamArchitectureDefinition]) {
  private val instanceRunning: mutable.Map[ServiceInstanceID, SamServiceInstance] = mutable.Map.empty
  private val configurations: mutable.Map[ServiceConfigurationID, InjectionConfiguration] = mutable.Map.empty
  private val exposedServicesUrl: mutable.Map[ServiceConfigurationID, LiftedServiceConfiguration] = mutable.Map.empty
  private val exposedServiceConfigurations: mutable.Map[ServiceInstanceURL, LiftedServiceConfiguration] = mutable.Map.empty
  val logger = LoggerFactory.getLogger(this.getClass)

  // Registration of implementations
  val implementationRegistry: Map[SamServiceImplementationKey, SamServiceImplementation] = Map() ++ {
    implementationContracts map {
      SamModelBuilder.loadImplementationPackage _
    } flatMap {
      _ map (s => s.implKey -> s)
    }
  }

  val architectures = architectureDefinitions map {
    SamModelBuilder.loadArchitectureDefinition _
  }

  val serviceByKey: Map[SamServiceKey, SamService] = Map() ++ {
    architectures flatMap {
      _.categories.flatMap(c => c.services).map(s => s.id -> s)
    }
  }

  def addServiceInstance(instance: SamServiceInstance) = instanceRunning.update(instance.instanceId, instance)

  def addInjectionConfiguration(configuration: InjectionConfiguration) = configurations.update(configuration.configurationId, configuration)

  def exposeServiceConfiguration(configurationId: ServiceConfigurationID, urlCreator: ServiceConfigurationID => ServiceInstanceURL): LiftedServiceConfiguration = {
    val exposed = exposedServicesUrl.get(configurationId) match {
      case Some(url) => url
      case None => {
        val url = urlCreator(configurationId)
        val lifted = LiftedServiceConfiguration(url,configurationId)
        exposedServicesUrl.put(configurationId, lifted)
        exposedServiceConfigurations.put(url, lifted)
        lifted
      }
    }
    exposed
  }

  def getRunningServiceInstances = instanceRunning

  def getConfigurations = configurations

  def getExposedServicesUrl = exposedServicesUrl

  def getExposedServiceConfigurations = exposedServiceConfigurations

}

private class SamInjectionTransaction(
      val architectureManager: SamArchitectureManagementApi,
                                       val status: ServiceExecutionEnvironmentStatus,
                                       val urlCreator: ServiceConfigurationID => ServiceInstanceURL
                                       ) extends SamInjectionTransactionApi {

  val logger = LoggerFactory.getLogger(this.getClass)
  val transactionIdGenerator = LongLongIdGenerator.createGenerator()

  def createExecutionContext(tid: LiftedServiceConfiguration) = {
    val config = status.getConfigurations(tid.configId)
    val transportContext = createTransactionTransportContext(config)
    val context = CanonicalRecordingLayer(architectureManager, config.configurationRoot, transportContext)
    logger.trace("created context for configuration {}", config.configurationRoot)
    context
  }

  private def createTransactionTransportContext(config: InjectionConfiguration): TransactionTransportContext = {
    val externalBind = InjectionTransaction.getExternalBind(config.configurationRoot)
    val localConfig = status.getExposedServiceConfigurations
    val externalPipes = externalBind map {
      case e@ExternalServiceBind(_, url) if localConfig.contains(url) => {
        val context = createExecutionContext(localConfig(url))
        (e, new DirectExecutionPipe(context))
      }
      case e@ExternalServiceBind(_, url) => (e, ???)
    }
    val loopBackPipe = new FakeTransportPipe()
    val input = new FakeTransportPipe()
    new TransactionTransportContext(externalPipes, input, loopBackPipe)
  }

  def liftServiceConfiguration(configurationId: ServiceConfigurationID): LiftedServiceConfiguration = {
    status.exposeServiceConfiguration(configurationId, urlCreator)
  }

  def executeServiceAction[R, T](configuration: LiftedServiceConfiguration, action: ServiceAction[R, T]): Future[R] = {
    val injectionExecutionContext = createExecutionContext(configuration)
    ThreadExecutionModel.openTransactionThread(injectionExecutionContext).executeServiceAction(action)
  }

}
