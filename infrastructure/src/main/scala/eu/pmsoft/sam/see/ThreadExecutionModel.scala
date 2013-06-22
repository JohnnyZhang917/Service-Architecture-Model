package eu.pmsoft.sam.see

import scala.concurrent._
import ExecutionContext.Implicits.global
import eu.pmsoft.sam.execution.ServiceAction
import org.slf4j.LoggerFactory


object ThreadExecutionModel {
  def openTransactionThread(context: InjectionTransactionContext) = new TransactionThreadStatus(context)
}


class TransactionThreadStatus(context: InjectionTransactionContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def executeServiceAction[R, T](action: ServiceAction[R, T]): Future[R] = {
    Future {
      context.bindTransaction(None)
      val serviceApi = context.getTransactionInjector.getInstance(action.getInterfaceKey)
      val result = action.executeInteraction(serviceApi)
      context.unBindTransaction
      result
    }
  }

  def executeCanonicalProtocolMessage(clientTransport: TransportAbstraction): Future[Unit] = {
    clientTransport.receive().map {
      rootMessage => {
        try {
          logger.trace("pre bind")
          context.bindTransaction(Option(clientTransport))
        } catch {
          case e: Throwable => {
            e.printStackTrace()
            throw e
          }
        } finally {
          logger.trace("post bind")
        }
        logger.trace("execute call")
        val rootResponse = context.protocolExecution(rootMessage)
        logger.trace("execution done")
        context.unBindTransaction
        rootResponse
      }
    }
  }.flatMap {
    response => clientTransport.send(response)
  }
}

