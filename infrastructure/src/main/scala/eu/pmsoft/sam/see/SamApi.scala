package eu.pmsoft.sam.see

import eu.pmsoft.sam.model._
import com.google.inject.Injector
import eu.pmsoft.sam.execution.ServiceAction
import scala.concurrent.Future
import java.net.InetSocketAddress
import eu.pmsoft.sam.idgenerator.LongLongID
import eu.pmsoft.sam.model.ExposedServiceTransaction
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.SamService
import eu.pmsoft.sam.model.SamServiceInstance
import eu.pmsoft.sam.model.SamServiceImplementationKey
import eu.pmsoft.sam.model.LiftedServiceConfiguration
import eu.pmsoft.sam.model.SamServiceImplementation

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

  // Register configuration
  def registerInjectionConfiguration(element: InjectionConfigurationElement): ServiceConfigurationID

  // Expose configuration to a generated URL
  def liftServiceConfiguration(configurationId: ServiceConfigurationID): LiftedServiceConfiguration
}


trait SamInjectionTransactionApi {
  // Create a global transaction and execute the service action on the head injector
  def executeServiceAction[R, T](configuration: LiftedServiceConfiguration, action: ServiceAction[R, T]): Future[R]
}

trait InjectionTransactionAccessApi {
  def getTransactionInjector: Injector

  def bindTransaction: Unit

  def unBindTransaction: Unit

  def getTransactionTransportContext: TransactionTransportContext

  def enterPendingMode(): Unit
}


trait SamEnvironmentExternalConnector {

  def onEnvironment(address: InetSocketAddress): SamEnvironmentExternalApi

  def getConnectionFromServiceURL(url: ServiceInstanceURL): SamEnvironmentExternalApi

  def openExecutionPipe(pipeID: PipeIdentifier, remoteEndpointRef: PipeReference): TransportPipe[ThreadMessage]

  def openLoopbackPipe(pipeID: PipeIdentifier, remoteEndpointRef: PipeReference): LoopTransportPipe[ThreadMessage]

}

trait SamEnvironmentExternalApi {
  def getArchitectureSignature(): Future[String]

  def ping(): Future[Boolean]

  def getExposedServices(): Future[Seq[ExposedServiceTransaction]]

  def registerTransaction(globalTransactionID: LongLongID, headConnectionPipe: PipeReference, service: ExposedServiceTransaction): Future[TransactionBindRegistration]

  def unRegisterTransaction(globalTransactionID: LongLongID): Future[Boolean]
}

case class TransactionBindRegistration(globalID: LongLongID, surl: ServiceInstanceURL, bindId: ThreadExecutionIdentifier, transactionHeadPipeRef: PipeReference)