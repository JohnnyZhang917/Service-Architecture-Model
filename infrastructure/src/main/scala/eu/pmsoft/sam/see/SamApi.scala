package eu.pmsoft.sam.see

import eu.pmsoft.sam.model._
import com.google.inject.Injector
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.SamService
import eu.pmsoft.sam.model.SamServiceInstance
import eu.pmsoft.sam.model.SamServiceImplementationKey
import eu.pmsoft.sam.model.SamServiceImplementation
import eu.pmsoft.sam.execution.ServiceAction
import scala.concurrent.Future
import java.net.InetSocketAddress

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
  def liftServiceConfiguration(configurationId: ServiceConfigurationID): LiftedServiceConfiguration

  def executeServiceAction[R, T](configuration: LiftedServiceConfiguration, action: ServiceAction[R, T]): Future[R]
}

trait InjectionTransactionAccessApi {
  def getTransactionInjector: Injector

  def bindTransaction: Unit

  def unBindTransaction: Unit
}


trait SamEnvironmentExternalConnector {
  def onEnvironment(address: InetSocketAddress): SamEnvironmentExternalApi
}

trait SamEnvironmentExternalApi {
  def getArchitectureSignature(): Future[String]
  def ping(): Future[Boolean]
}