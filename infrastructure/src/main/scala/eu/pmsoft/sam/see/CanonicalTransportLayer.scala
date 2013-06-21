package eu.pmsoft.sam.see

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model._
import java.util.concurrent.{TimeUnit, BlockingQueue, LinkedBlockingQueue}
import scala.util.{Failure, Success}

object CanonicalTransportLayer {
  def apply(port: Int) = new CanonicalTransportServer(port)
}

case class CanonicalProtocolMessage(instances: Seq[CanonicalProtocolInstance], calls: Seq[CanonicalProtocolMethodCall], closeHomotopy: Boolean)

trait SlotExecutionPipe {
  def openPipe(): ExecutionPipe
}

trait LoopBackExecutionPipe extends SlotExecutionPipe {
  def bindTransportContext(context : TransportAbstraction)
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

class DisabledExecutionPipe extends ExecutionPipe {
  def transport(message: ThreadMessage): Future[ThreadMessage] = throw new IllegalAccessException()
}

private trait TransportAbstraction {
  def send(message: ThreadMessage): Future[Unit]

  def receive(): Future[ThreadMessage]
}

private[see] class CanonicalTransportServer(val port: Int) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val serverAddress = new InetSocketAddress(port)
  //  val serverBootstrap = new ServerBootstrap()
  //
  //  serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
  //    .channel(classOf[NioServerSocketChannel])
  //    .localAddress(serverAddress)
  //    .childHandler(new ChannelInitializer[SocketChannel]() {
  //    def initChannel(ch: SocketChannel) {
  //      ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE)).addLast(new WriteTimeoutHandler(3000)).addLast(new ReadTimeoutHandler(3000))
  //        .addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), new ServerHandler());
  //    }
  //  })
  //
  //  val binding = serverBootstrap.bind()
  //
  //  binding.addListener(new ChannelFutureListener() {
  //    def operationComplete(p1: ChannelFuture) {
  //      logger.debug("server bind finished")
  //    }
  //  })

}

private class TMPSlotExecutionPipe extends LoopBackExecutionPipe with ExecutionPipe {

  val transportRef : ThreadLocal[TransportAbstraction] = new ThreadLocal[TransportAbstraction]()

  def bindTransportContext(context: TransportAbstraction) {
    transportRef.set(context)
  }

  def unbindTransportContext() {
    transportRef.remove()
  }

  def openPipe(): ExecutionPipe = this

  def transport(message: ThreadMessage): Future[ThreadMessage] = {
    val transport = transportRef.get()
    val sended = transport.send(message)
    sended.flatMap {
      ok => transport.receive()
    }

  }
}

private class DirectExecutionPipe(val transaction: InjectionTransactionContext) extends SlotExecutionPipe with ExecutionPipe {
  val executionThread = ThreadExecutionModel.openTransactionThread(transaction)
  val localTransportFake = new LocalJVMSingleMessageTransport

  def openPipe(): ExecutionPipe = this

  def transport(message: ThreadMessage): Future[ThreadMessage] = {

    localTransportFake.clientView.send(message).flatMap {
      _ => localTransportFake.providerView.receive()
    }.flatMap {
      rootMessage => executionThread.executeCanonicalProtocolMessage(rootMessage, localTransportFake.providerView)
    }

    localTransportFake.clientView.receive()

  }

}

private class LocalJVMSingleMessageTransport {
  private val clientToProvider: BlockingQueue[ThreadMessage] = new LinkedBlockingQueue[ThreadMessage](1)
  private val providerToClient: BlockingQueue[ThreadMessage] = new LinkedBlockingQueue[ThreadMessage](1)

  val clientView: TransportAbstraction = new TransportAbstraction() {
    def send(message: ThreadMessage): Future[Unit] = Future {
      clientToProvider.add(message)
    }

    def receive(): Future[ThreadMessage] = Future {
      providerToClient.take()
    }
  }

  val providerView: TransportAbstraction = new TransportAbstraction() {
    def send(message: ThreadMessage): Future[Unit] = Future {
      providerToClient.add(message)
    }

    def receive(): Future[ThreadMessage] = Future {
      clientToProvider.take()
    }
  }

}


//case class CanonicalProtocolMessageSerialization(data: Array[Byte])
//
//
//private class ServerHandler extends ChannelInboundMessageHandlerAdapter[CanonicalProtocolMessageSerialization] {
//
//  val logger = LoggerFactory.getLogger(this.getClass)
//
//  logger.debug("server handler init!!!")
//
//  def messageReceived(ctx: ChannelHandlerContext, message: CanonicalProtocolMessageSerialization) {
//    logger.debug("message {}", message)
//  }
//
//}
//
