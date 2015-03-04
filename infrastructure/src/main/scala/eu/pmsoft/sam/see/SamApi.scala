/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def exitPendingMode(): Unit
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

  def registerTransactionRemote(globalTransactionID: GlobalTransactionIdentifier, headConnectionPipe: PipeReference, service: ExposedServiceTransaction): Future[TransactionBindRegistration]

  def unRegisterTransactionRemote(globalTransactionID: GlobalTransactionIdentifier): Future[Boolean]
}

case class TransactionBindRegistration(globalID: GlobalTransactionIdentifier, surl: ServiceInstanceURL, bindId: ThreadExecutionIdentifier, transactionHeadPipeRef: PipeReference)