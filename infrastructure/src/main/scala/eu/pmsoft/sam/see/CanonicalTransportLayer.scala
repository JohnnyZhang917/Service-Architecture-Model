package eu.pmsoft.sam.see

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model._
import java.util.concurrent.LinkedBlockingQueue
import scala.util.{Failure, Success}
import eu.pmsoft.sam.see.netty.{NettyTransport, Result}
import eu.pmsoft.sam.idgenerator.LongLongID
import com.twitter.concurrent.AsyncQueue

object CanonicalTransportLayer {

}

trait CanonicalProtocolCommunicationApi {

  def getEnvironmentConnection(host: String, port: Int): EnvironmentConnection
}

trait EnvironmentConnection {

  def ping: Future[Result]

  def protocolAction(transactionId: LongLongID, message: ThreadMessage): Future[ThreadMessage]

  def openPipeTransaction(url: ServiceInstanceURL): Future[LongLongID]

}


private trait TransportAbstraction {
  def send(message: ThreadMessage): Future[Unit]

  def receive(): Future[ThreadMessage]
}


case class CanonicalProtocolMessage(instances: Seq[CanonicalProtocolInstance], calls: Seq[CanonicalProtocolMethodCall], closeHomotopy: Boolean)

trait LoopBackExecutionPipe extends ExecutionPipe {
  def bindTransportContext(context: TransportAbstraction)

  def unbindTransportContext()
}

trait ExecutionPipe {
  val logger = LoggerFactory.getLogger(this.getClass)
  private val queue = new LinkedBlockingQueue[ThreadMessage]()

  def transport(message: ThreadMessage): Future[ThreadMessage]

  def waitResponse: ThreadMessage = queue.take()

  def pollResponse: Option[ThreadMessage] = Option(queue.poll())

  def sendMessage(message: ThreadMessage): Unit = {
    val fres = transport(message)
    fres onComplete {
      case Success(res) => {
        logger.debug("message success {}", res)
        queue.add(res)
      }
      case Failure(t) => {
        logger.debug("message failure {}", t)
        queue.add(ThreadMessage(None, Some(t)))
      }
    }
  }
}


private[see] class CanonicalTransportServer(val transactionApi: SamInjectionTransaction, val port: Int) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val serverAddress = new InetSocketAddress(port)
  val environmentServer = NettyTransport.createNettyServer(transactionApi, serverAddress)

  val transportApi = NettyTransport.api()

  def getExecutionPipe(url: ServiceInstanceURL): ExecutionPipe = {
    new ExternalTransportPipeImplementation(url, transportApi.getEnvironmentConnection(url.url.getHost, url.url.getPort))
  }
}


private class ExternalTransportPipeImplementation(url: ServiceInstanceURL, connection: EnvironmentConnection) extends ExecutionPipe {

  val transactionPipeID: Future[LongLongID] = connection.openPipeTransaction(url)

  def transport(message: ThreadMessage): Future[ThreadMessage] = transactionPipeID flatMap {
    tid => {
      connection.protocolAction(tid, message)
    }
  }
}

private class TMPSlotExecutionPipe extends LoopBackExecutionPipe with ExecutionPipe {

  val transportRef: ThreadLocal[TransportAbstraction] = new ThreadLocal[TransportAbstraction]()

  def bindTransportContext(context: TransportAbstraction) {
    logger.debug("bind transport to pipe")
    transportRef.set(context)
  }

  def unbindTransportContext() {
    logger.debug("unbind transport to pipe")
    transportRef.remove()
  }

  def openPipe(): ExecutionPipe = this

  def transport(message: ThreadMessage): Future[ThreadMessage] = {
    assert(transportRef.get() != null)
    val transport = transportRef.get()
    transport.send(message).flatMap {
      ok => transport.receive()
    }
  }
}

private class DirectExecutionPipe(val transaction: InjectionTransactionContext) extends ExecutionPipe {
  val executionThread = ThreadExecutionModel.openTransactionThread(transaction)
  val localTransportFake = new LocalJVMSingleMessageTransport

  def transport(message: ThreadMessage): Future[ThreadMessage] = {
    logger.debug("sending")
    val sendOk = localTransportFake.clientView.send(message)
    sendOk.flatMap {
      ok => {
        if (!message.data.get.closeThread) {
          executionThread.executeCanonicalProtocolMessage(localTransportFake.providerView)
        } else {
          Future {}
        }
      }
    }
    sendOk.flatMap {
      ok => {
        logger.debug("sendOk, wait on client for response")
        localTransportFake.clientView.receive()
      }
    }
  }

}

private class LocalJVMSingleMessageTransport {
  private val clientToProvider: AsyncQueue[ThreadMessage] = new AsyncQueue[ThreadMessage]()
  private val providerToClient: AsyncQueue[ThreadMessage] = new AsyncQueue[ThreadMessage]()

  val logger = LoggerFactory.getLogger(this.getClass)
  val clientView: TransportAbstraction = new TransportAbstraction() {


    def send(message: ThreadMessage): Future[Unit] = Future {
      logger.trace("C ADD")
      clientToProvider.offer(message)
    }

    def receive(): Future[ThreadMessage] = {
      logger.trace("P MINUS")
      providerToClient.poll()
    }
  }

  val providerView: TransportAbstraction = new TransportAbstraction() {
    def send(message: ThreadMessage): Future[Unit] = Future {
      logger.trace("P ADD")
      providerToClient.offer(message)
    }

    def receive(): Future[ThreadMessage] = {
      logger.trace("C MINUS")
      clientToProvider.poll()
    }
  }

}
