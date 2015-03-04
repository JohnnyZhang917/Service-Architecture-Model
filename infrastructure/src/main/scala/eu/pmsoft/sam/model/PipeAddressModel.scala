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

package eu.pmsoft.sam.model

import eu.pmsoft.sam.idgenerator.LongLongID
import eu.pmsoft.sam.see.ThreadMessage

object PipeAddressModel {

}


case class GlobalTransactionIdentifier(globalID: LongLongID)

case class ThreadExecutionIdentifier(tid: LongLongID)

case class PipeIdentifier(pid: LongLongID)

case class PipeReference(transactionBindID: ThreadExecutionIdentifier, pipeID: PipeIdentifier, address: ServiceInstanceURL)

case class MessageRoutingInformation(sourcePipeID: PipeIdentifier, remoteTargetPipeRef: PipeReference, message: ThreadMessage)

case class TransactionBinding(globalId: GlobalTransactionIdentifier,
                              tid: ThreadExecutionIdentifier,
                              localHeadPipeId: PipeIdentifier,
                              clientPipeReference: PipeReference,
                              internalBind: Map[ServiceInstanceURL, PipeIdentifier],
                              externalBinds: Map[ServiceInstanceURL, PipeReference])


case class TransactionRegistrationResult(globalTransactionID: GlobalTransactionIdentifier, transactionID: ThreadExecutionIdentifier, transactionHeadPipeReference: PipeReference)

trait TransportPipe[T] {

  def waitResponse: T

  def pollResponse: Option[T]

  def sendMessage(message: T): Unit

  def receiveMessage(message: T): Unit

  def getPipeID: PipeIdentifier

  def isWaitingForMessage: Boolean
}

trait LoopTransportPipe[T] {
  def getInputPipe: TransportPipe[T]

  def getLoopBackPipe: TransportPipe[T]

}

