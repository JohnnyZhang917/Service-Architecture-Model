package eu.pmsoft.sam.see

import eu.pmsoft.sam.model._
import com.google.inject._
import eu.pmsoft.sam.model.ServiceInstanceID
import eu.pmsoft.sam.model.SamServiceImplementation
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.SamService
import eu.pmsoft.sam.model.SamServiceInstance
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalBindingController, ExtrenalBindingInfrastructureModule}
import scala.collection.mutable
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract

object ServiceExecutionEnvironment {

  def apply(configuration: SEEConfiguration) = new ServiceExecutionEnvironment(configuration)

  def configurationBuilder(port: Int) = SEEConfigurationBuilder(SEEConfiguration(Set(), Set(), port))
}

class ServiceExecutionEnvironment(val configuration: SEEConfiguration) {

  private val status = new ServiceExecutionEnvironmentStatus(configuration.implementations, configuration.architectures)
  private val server = new CanonicalTransportServer(configuration.port)
  private val transport = new SamCanonicalTransportLayer(status, server)

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

case class SEEConfiguration(
                             architectures: Set[SamArchitectureDefinition],
                             implementations: Set[SamServiceImplementationPackageContract],
                             port: Int)

case class SEEConfigurationBuilder(config: SEEConfiguration) {

  def withArchitecture(toAdd: SamArchitectureDefinition) = new SEEConfigurationBuilder(config.copy(architectures = config.architectures + toAdd))

  def withImplementation(toAdd: SamServiceImplementationPackageContract) = new SEEConfigurationBuilder(config.copy(implementations = config.implementations + toAdd))

  def build = config

}

trait SamArchitectureManagementApi {

  def getService(key: SamServiceKey): SamService
}

trait SamServiceRegistryApi {

  def getImplementation(key: SamServiceImplementationKey): SamServiceImplementation
}

trait SamExecutionNodeApi {

  def createServiceInstance(key: SamServiceImplementationKey): SamServiceInstance

  def getServiceInstances(key: SamServiceImplementationKey): Set[ServiceInstanceID]

  def getInstance(id: ServiceInstanceID): SamServiceInstance

  def registerInjectionConfiguration(element: InjectionConfigurationElement): ServiceConfigurationID

}

trait SamInjectionTransactionApi {

  def liftServiceConfiguration(configurationId: ServiceConfigurationID): ServiceInstanceURL

  def getTransaction(configurationId: ServiceConfigurationID): InjectionTransaction

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

  def createServiceInstance(key: SamServiceImplementationKey): SamServiceInstance = {
    val implementation = serviceRegistry.getImplementation(key)
    val instanceInjector: Injector = createServiceInjector(implementation)
    val instance = SamServiceInstance(ServiceInstanceID(), implementation, instanceInjector)
    status.addServiceInstance(instance)
    instance
  }

  private def createServiceInjector(implementation: SamServiceImplementation): Injector = {
    val moduleInstance = implementation.implKey.module.newInstance()
    val serviceContracts: Seq[Set[Key[_]]] = implementation.bindServices.map(architectureManager.getService _).map(_.api)
    Guice.createInjector(moduleInstance, FreeBindingBuilder.buildFreeBindingModule(serviceContracts))
  }

  def getServiceInstances(key: SamServiceImplementationKey): Set[ServiceInstanceID] = {
    status.getRunningServiceInstances.filter(_._2.implementation.implKey == key).map(_._1).toSet
  }

  def getInstance(id: ServiceInstanceID): SamServiceInstance = status.getRunningServiceInstances.getOrElse(id, null)

  def registerInjectionConfiguration(element: InjectionConfigurationElement): ServiceConfigurationID = {
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
        contracts.view.zipWithIndex.foreach {
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
    exposedServicesUrl.get(configurationId) match {
      case Some(url) => url
      case None => {
        val url = urlCreator(configurationId)
        exposedServicesUrl.put(configurationId, url)
        exposedServiceConfigurations.put(url, configurationId)
        url
      }
    }
  }

  def getRunningServiceInstances = instanceRunning

  def getConfigurations = configurations

  def getExposedServicesUrl = exposedServicesUrl

  def getExposedServiceConfigurations = exposedServiceConfigurations

}

private class SamCanonicalTransportLayer(
                                          val status: ServiceExecutionEnvironmentStatus,
                                          val server: CanonicalTransportServer
                                          ) extends SamInjectionTransactionApi {

  private def createExecutionContext(tid: ServiceConfigurationID): InjectionTransactionExecutionContext = {
    val config = status.getConfigurations(tid)
    val contextBuilder = CanonicalRecordingLayer(createExternalServiceConnections _)
    contextBuilder.createContext(config.configurationRoot)
  }

  def createUrl(configurationId: ServiceConfigurationID): ServiceInstanceURL = {
    ServiceInstanceURL(new java.net.URL("http", server.serverAddress.getHostName, server.serverAddress.getPort, s"/service/${configurationId.id}"))
  }

  def createExternalServiceConnections(endpoints: Seq[ExternalServiceBind]): Seq[SlotExecutionPipe] = {
    val localConfig = status.getExposedServiceConfigurations
    endpoints.map {
      case ExternalServiceBind(_, url) if localConfig.contains(url) => {
        val context = createExecutionContext(localConfig(url))
        new DirectExecutionPipe(context.executionContext.serverExecutionManager)
      }
      case _ => new TMPSlotExecutionPipe
    }
  }

  def liftServiceConfiguration(configurationId: ServiceConfigurationID): ServiceInstanceURL = status.exposeServiceConfiguration(configurationId, createUrl)

  def getTransaction(tid: ServiceConfigurationID): InjectionTransaction = {
    createExecutionContext(tid).transaction
  }


}

//private class SamInjectionTransaction(val transport: SamCanonicalTransportLayer, val status: ServiceExecutionEnvironmentStatus) extends SamInjectionTransactionApi {
//
//  def liftServiceConfiguration(configurationId: ServiceConfigurationID): ServiceInstanceURL = status.exposeServiceConfiguration(configurationId, transport.createUrl)
//
//  def getTransaction(tid: ServiceConfigurationID): InjectionTransaction = {
//    val contextBuilder: InjectionTransactionContextBuilder = CanonicalRecordingLayer( transport.createExternalServiceConnections _ )
//    val transaction = status.getConfigurations.get(tid) map {
//      c => InjectionTransaction(c.configurationRoot, contextBuilder)
//    }
//    transaction.getOrElse(null)
//  }
//
//}
