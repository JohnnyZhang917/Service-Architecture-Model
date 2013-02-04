package eu.pmsoft.sam.see

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.net.{URL, InetSocketAddress}
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model._
import java.util.concurrent.LinkedBlockingQueue
import scala.util.Success
import scala.util.Failure
import scala.Some

import eu.pmsoft.sam.transport._
import eu.pmsoft.sam.idgenerator.LongLongID

object CanonicalTransportLayer {

  private val EnvProtoCodec = TransportCodecBuilder.clientProtobuff[SamEnvironmentAction, SamEnvironmentResult](SamEnvironmentResult.getDefaultInstance)

  def createClientDispatcher(address: InetSocketAddress) : Future[RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult]] = {
    val client = ClientConnector[SamEnvironmentAction, SamEnvironmentResult](EnvProtoCodec)
    client.connect(address) map {
      transport => RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult](transport)
    }
  }
}


case class CanonicalProtocolMessage(instances: Seq[CanonicalProtocolInstance], calls: Seq[CanonicalProtocolMethodCall], closeHomotopy: Boolean)


trait TransportPipe {

  def waitResponse: ThreadMessage

  def pollResponse: Option[ThreadMessage]

  def sendMessage(message: ThreadMessage): Unit

}

class FakeTransportPipe extends TransportPipe {
  def waitResponse: ThreadMessage = ???

  def pollResponse: Option[ThreadMessage] = ???

  def sendMessage(message: ThreadMessage) {
    ???
  }
}

private trait ExecutionPipe extends TransportPipe {
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

private[see] class CanonicalTransportServer(val logic: EnvironmentExternalApiLogic, val port: Int) extends SamEnvironmentExternalConnector {
  val logger = LoggerFactory.getLogger(this.getClass)
  val serverAddress = new InetSocketAddress(port)

  private val server = ServerConnector[SamEnvironmentAction, SamEnvironmentResult](
    serverAddress,
    TransportCodecBuilder.serverProtobuff[SamEnvironmentAction, SamEnvironmentResult](SamEnvironmentAction.getDefaultInstance),
    onConnection
  )

  val channel = server.bind

  def onConnection(transport: Transport[SamEnvironmentResult, SamEnvironmentAction]) {
    // TODO Disconect and clean
    new SerialServerDispatcher(transport, logic.handle _ )
  }

  def createUrl(configurationId: ServiceConfigurationID): ServiceInstanceURL = {
    logger.trace("createUrl {}", configurationId)
    ServiceInstanceURL(new java.net.URL("http", serverAddress.getHostName, serverAddress.getPort, s"/service/${configurationId.id}"))
  }

  def onEnvironment(address: InetSocketAddress): SamEnvironmentExternalApi = new EnvironmentConnection(logic, CanonicalTransportLayer.createClientDispatcher(address))
}

private class EnvironmentConnection(val logic: EnvironmentExternalApiLogic, val dispatcher: Future[RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult]]) extends SamEnvironmentExternalApi {

  def getArchitectureSignature(): Future[String] = dispatcher flatMap { disp =>
    val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.ARCHITECTURE_INFO)
    disp.dispatch(action)
  } map { _.`architectureInfoSignature`.get }

  def ping(): Future[Boolean] = dispatcher flatMap { disp =>
    val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.PING)
    disp.dispatch(action)
  } map { res => true }

  def getExposedServices: Future[Seq[ExposedServiceTransaction]] =  dispatcher flatMap { disp =>
    val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.SERVICE_LIST)
    disp.dispatch(action)
  } map { res =>
     res.`serviceList` map { ref =>
       ExposedServiceTransaction(logic.createUrl(ref.`url`), logic.findServiceContract(ref.`contract`))
     }
  }

  def registerTransaction(tid: LongLongID, service: ExposedServiceTransaction): Future[Boolean] = ???

  def unRegisterTransaction(tid: LongLongID): Future[Boolean] = ???
}


private trait TransportAbstraction {
  def send(message: ThreadMessage): Future[Unit]

  def receive(): Future[ThreadMessage]
}

private class DirectExecutionPipe(val transaction: InjectionTransactionAccessApi) extends ExecutionPipe {
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
