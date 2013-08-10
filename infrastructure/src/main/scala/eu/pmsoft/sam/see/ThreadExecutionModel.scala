package eu.pmsoft.sam.see

import scala.concurrent._
import eu.pmsoft.sam.execution.ServiceAction
import org.slf4j.LoggerFactory
import scala.util.{Failure, Success}
import java.util.concurrent.Executors


object ThreadExecutionModel {

  def openTransactionThread(context: InjectionTransactionAccessApi) = new TransactionThreadStatus(context)

}

object ThreadExecutionContext {
  implicit lazy val possibleBlockingExecutor: ExecutionContextExecutor = {
    val service = Executors.newCachedThreadPool()
    ExecutionContext.fromExecutorService(service)
  }
}


class TransactionThreadStatus(context: InjectionTransactionAccessApi) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  import ThreadExecutionContext._

  def executeServiceAction[R, T](action: ServiceAction[R, T]): Future[R] = {
    Future {
      context.bindTransaction
      val serviceApi = context.getTransactionInjector.getInstance(action.getInterfaceKey)
      val result = action.executeInteraction(serviceApi)
      context.unBindTransaction
      result
    }
  }

  def executeCanonicalProtocolMessage(clientTransport: TransportAbstraction): Future[Unit] = {
    ???
//    logger.trace("execution on transaction thread starting on client receive")
//    val process = clientTransport.receive().map {
//      rootMessage => {
//        try {
//          context.bindTransaction
////          val rootResponse = context.protocolExecution(rootMessage)
//          val rootResponse = ???
//          //TODO
//          logger.trace("execution done")
//          context.unBindTransaction
//          rootResponse
//        } catch {
//          case e: Throwable => {
//            e.printStackTrace()
//            throw e
//          }
//        } finally {
//          logger.trace("post bind")
//        }
//      }
//    }.flatMap {
//      response => clientTransport.send(response)
//    }
//    process.onComplete {
//      case Success(r) => {
//        logger.trace("external bind success")
//      }
//      case Failure(t) => {
//        logger.trace("external bind failed")
//        t.printStackTrace()
//      }
//    }
//    process
  }
}

