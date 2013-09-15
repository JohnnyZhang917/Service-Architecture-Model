package eu.pmsoft.sam.model

import eu.pmsoft.sam.idgenerator.LongLongID
import eu.pmsoft.sam.see.ThreadMessage

object PipeAddressModel {

}


case class ThreadExecutionIdentifier(tid: LongLongID)

case class PipeIdentifier(pid: LongLongID)

case class PipeReference(transactionBindID: ThreadExecutionIdentifier, pipeID: PipeIdentifier, address: ServiceInstanceURL)

case class MessageRoutingInformation(sourcePipeID: PipeIdentifier, remoteTargetPipeRef: PipeReference, message: ThreadMessage)

case class TransactionBinding(globalId: LongLongID,
                              tid: ThreadExecutionIdentifier,
                              localHeadPipeId: PipeIdentifier,
                              clientPipeReference: PipeReference,
                              internalBind: Map[ServiceInstanceURL, PipeIdentifier],
                              externalBinds: Map[ServiceInstanceURL, PipeReference])


case class TransactionRegistrationResult(transactionID: ThreadExecutionIdentifier, transactionHeadPipeReference: PipeReference)

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

