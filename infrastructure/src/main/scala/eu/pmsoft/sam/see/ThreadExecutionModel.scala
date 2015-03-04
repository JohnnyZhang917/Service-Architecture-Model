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

import scala.concurrent._
import eu.pmsoft.sam.execution.ServiceAction
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import eu.pmsoft.sam.model.{MessageRoutingInformation, ThreadExecutionIdentifier}


object ThreadExecutionModel {

  def openTransactionThread(id: ThreadExecutionIdentifier, transactionInjector: InjectionTransactionAccessApi): TransactionThreadStatus = new TransactionThreadStatus(id, transactionInjector)

}

object ThreadExecutionContext {
  implicit lazy val possibleBlockingExecutor: ExecutionContextExecutor = {
    val service = Executors.newCachedThreadPool()
    ExecutionContext.fromExecutorService(service)
  }
}


class TransactionThreadStatus(id: ThreadExecutionIdentifier, transactionInjector: InjectionTransactionAccessApi) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  logger.debug("TransactionThreadStatus created")

  import ThreadExecutionContext._

  def executeServiceAction[R, T](action: ServiceAction[R, T]): Future[R] = {
    Future {
      logger.debug("Transaction Thread running for service interaction")
      transactionInjector.bindTransaction
      val serviceApi = transactionInjector.getTransactionInjector.getInstance(action.getInterfaceKey)
      val result = action.executeInteraction(serviceApi)
      transactionInjector.unBindTransaction
      result
    }
  }

  def startPendingThreadForExecution(): Future[Unit] = {
    Future {
      logger.debug("Transaction Thread running on pending mode ")
      transactionInjector.bindTransaction
      transactionInjector.enterPendingMode()
      transactionInjector.unBindTransaction
      logger.debug("Transaction Thread pending mode stopped ")
    }
  }

  def exitPendingMode() : Unit = {
    transactionInjector.exitPendingMode()
  }

  def dispatchMessage(routingInfo: MessageRoutingInformation): Future[Unit] = {
    Future {
      try {
        logger.debug("dispatch  to pipe {}", routingInfo.remoteTargetPipeRef)
        val inputPipe = transactionInjector.getTransactionTransportContext.loopPipe.getInputPipe
        val loopback = transactionInjector.getTransactionTransportContext.loopPipe.getLoopBackPipe
        val targetPipe = if (inputPipe.getPipeID == routingInfo.remoteTargetPipeRef.pipeID) {
          if (loopback.isWaitingForMessage) loopback else inputPipe
        } else {
          transactionInjector.getTransactionTransportContext.pipeBindMapping.get(routingInfo.remoteTargetPipeRef.pipeID).get
        }
        targetPipe.receiveMessage(routingInfo.message)
      } catch {
        case e: Exception => logger.error("dispatch failure", e)
      }
    }
  }

}

