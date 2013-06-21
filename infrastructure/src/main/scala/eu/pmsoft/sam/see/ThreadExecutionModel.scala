package eu.pmsoft.sam.see

import scala.concurrent._
import ExecutionContext.Implicits.global
import eu.pmsoft.sam.execution.ServiceAction


object ThreadExecutionModel {
  def openTransactionThread(context: InjectionTransactionContext) = new TransactionThreadStatus(context)
}


class TransactionThreadStatus(context: InjectionTransactionContext) {

  def executeServiceAction[R, T](action: ServiceAction[R, T]): Future[R] = {
    Future {
      context.bindTransaction
      val serviceApi = context.getTransactionInjector.getInstance(action.getInterfaceKey)
      val result = action.executeInteraction(serviceApi)
      context.unBindTransaction
      result
    }
  }

  def executeCanonicalProtocolMessage(rootMessage: ThreadMessage, clientTransport: TransportAbstraction): Future[Unit] = {
    context.bindTransaction
    val rootResponse = context.protocolExecution(rootMessage, clientTransport)
    context.unBindTransaction
    clientTransport.send(rootResponse)
  }

}
