package eu.pmsoft.sam.see.netty

import scala.util._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import ExecutionContext.Implicits.global
import java.util.concurrent.{ConcurrentHashMap, CancellationException}
import java.io.{ByteArrayOutputStream, IOException, ObjectOutputStream, ObjectInputStream}
import java.net.InetSocketAddress
import io.netty.channel._
import io.netty.bootstrap.{ServerBootstrap, Bootstrap}
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioSocketChannel}
import io.netty.channel.socket.SocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import org.slf4j.LoggerFactory
import com.google.inject.Key
import com.google.protobuf.ByteString
import eu.pmsoft.sam.see.netty.NettyFutureTranslation._
import eu.pmsoft.sam.see._
import eu.pmsoft.sam.idgenerator.{LongLongIdGenerator, LongLongID}
import eu.pmsoft.sam.model._
import eu.pmsoft.sam.model.CanonicalProtocolInstanceProto.InstanceType
import eu.pmsoft.sam.model.CanonicalProtocolMethodCallProto.CallType


object NettyTransport {

  def createNettyServer(transactionApi: SamInjectionTransaction, address: InetSocketAddress) = new NettyCanonicalProtocolServer(transactionApi, address)

  def api(): CanonicalProtocolCommunicationApi = new NettyCanonicalProtocolCommunicationApi()

}


class NettyCanonicalProtocolServer(transactionApi: SamInjectionTransaction, address: InetSocketAddress) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val server = new ServerBootstrap()
  server
    .group(NettyUtils.bossGroup, NettyUtils.workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .option(ChannelOption.SO_BACKLOG, java.lang.Integer.valueOf(100))
    .option(ChannelOption.TCP_NODELAY, java.lang.Boolean.valueOf(true))
    .option(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.valueOf(true))
    .handler(new LoggingHandler(LogLevel.INFO))
    .childHandler(new ChannelInitializer[SocketChannel] {
    def initChannel(ch: SocketChannel) {
      ch.pipeline().addLast(
        //        new LoggingHandler(LogLevel.INFO),
        new ObjectEncoder(),
        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
        new NettyServerHandler(transactionApi)
      )
    }
  })

  val channel: Future[Channel] = server.bind(address)

  channel.flatMap {
    c => c.closeFuture()
  } onComplete {
    _ match {
      case Failure(exception) => logger.error("exception", exception)
      case Success(value) => logger.debug(" channel closed")
    }
  }
}


object NettyFutureTranslation {
  implicit def futureFromNetty(channelFuture: ChannelFuture): Future[io.netty.channel.Channel] = {
    val promise = Promise[io.netty.channel.Channel]()
    channelFuture.addListener(new ChannelFutureListener {
      def operationComplete(p1: ChannelFuture) {
        promise complete Try(
          if (p1.isSuccess) p1.channel()
          else if (p1.isCancelled) throw new CancellationException
          else throw p1.cause()
        )
      }
    })
    promise.future
  }
}

private object NettyUtils {
  val bossGroup: NioEventLoopGroup = new NioEventLoopGroup()
  val workerGroup: NioEventLoopGroup = new NioEventLoopGroup()
  val clientGroup: NioEventLoopGroup = new NioEventLoopGroup()
}

private class NettyCanonicalProtocolCommunicationApi extends CanonicalProtocolCommunicationApi {
  val connectionMap: mutable.Map[String, EnvironmentConnection] = mutable.Map.empty

  def getEnvironmentConnection(host: String, port: Int): EnvironmentConnection = {
    val key = host + port
    connectionMap.getOrElse(key, {
      val connection = new NettyEnvironmentConnection(host, port)
      connectionMap.put(key, connection)
      connection
    })
  }

}

private class NettyEnvironmentConnection(val host: String, val port: Int) extends EnvironmentConnection {
  val logger = LoggerFactory.getLogger(this.getClass)
  // Single connection implementation
  val b = new Bootstrap()
  b.group(NettyUtils.clientGroup)
    .channel(classOf[NioSocketChannel])
    .option(ChannelOption.TCP_NODELAY, java.lang.Boolean.valueOf(true))
    .option(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.valueOf(true))
    .handler(new ChannelInitializer[SocketChannel] {
    def initChannel(ch: SocketChannel) {
      ch.pipeline().addLast(
        new LoggingHandler(LogLevel.INFO),
        new ObjectEncoder(),
        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
        new NettyClientHandler()
      )
    }
  })

  val connected: Future[Channel] = b.connect(host, port)

  connected onFailure {
    case err => logger.error("Error on connection setup", err)
  }
  connected onComplete {
    _ match {
      case Failure(exception) => logger.error("Error on connection setup", exception)
      case Success(value) => logger.debug("connection ready")
    }
  }

  val connection = connected.map(c => new Connection(c))

  def ping: Future[Result] = connection.flatMap {
    c => c.send[PingAction, PingResult](PingAction())
  }

  def protocolAction(transactionId: LongLongID, message: ThreadMessage): Future[ThreadMessage] = connection.flatMap {
    c => c.send[ProtocolMessageAction, ProtocolMessageResult](ProtocolMessageAction(translateToProto(transactionId, message)))
  }.map {
    response => translateFromProto(response.message)
  }

  def openPipeTransaction(url: ServiceInstanceURL): Future[LongLongID] = connection.flatMap {
    c => c.send[OpenTransactionAction, OpenTransactionResult](OpenTransactionAction(url))
  }.map {
    response => response.id
  }


  private def translateToProto(transactionId: LongLongID, message: ThreadMessage): CanonicalRequestProto = {
    def idMapper(llid: LongLongID): LongLongIDProto = {
      LongLongIDProto(llid.getMark, llid.getLinear)
    }
    def keyMapper(key: Key[_]): BindingKey = {
      BindingKey(
        Some(key.getTypeLiteral.getRawType.toString),
        Option(key.getAnnotationType()).map(_.toString),
        Option(key.getAnnotation).map(ano => dataMapper(ano.asInstanceOf[java.io.Serializable]))
      )
    }

    def dataMapper(serializable: java.io.Serializable): com.google.protobuf.ByteString = {
      val bos = new ByteArrayOutputStream()
      val out = new ObjectOutputStream(bos)
      try {
        out.writeObject(serializable)
        ByteString.copyFrom(bos.toByteArray)
      } catch {
        case e: Throwable => ???
      } finally {
        out.close()
        bos.close()
      }
    }
    message.data match {
      case None => CanonicalRequestProto()
      case Some(data) => {
        val protocalls = data.calls.map {
          _ match {
            case VoidMethodCall(instanceNr, methodSignature, arguments) => CanonicalProtocolMethodCallProto(instanceNr, methodSignature, None, arguments.toVector, CallType.VOID_CALL)
            case InterfaceMethodCall(instanceNr, methodSignature, arguments, returnNr) => CanonicalProtocolMethodCallProto(instanceNr, methodSignature, Some(returnNr), arguments.toVector, CallType.INTERFACE_CALL)
            case ReturnMethodCall(instanceNr, methodSignature, arguments, returnNr) => CanonicalProtocolMethodCallProto(instanceNr, methodSignature, Some(returnNr), arguments.toVector, CallType.RETURN_CALL)
          }
        }
        val protoInstances = data.instances.map {
          _ match {
            case ClientBindingKeyInstance(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.CLIENT_BINDING_KEY, keyMapper(key), None)
            case ClientReturnBindingKeyInstance(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.CLIENT_RETURN_BINDING_KEY, keyMapper(key), None)
            case ClientExternalInstanceBinding(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.CLIENT_EXTERNAL_INSTANCE_BINDING, keyMapper(key), None)
            case ClientPendingDataInstance(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.CLIENT_PENDING_DATA_INSTANCE, keyMapper(key), None)
            case ClientFilledDataInstance(instanceNr, key, data) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.CLIENT_FILLED_DATA_INSTANCE, keyMapper(key), Some(dataMapper(data)))
            case ClientDataInstance(instanceNr, key, data) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.CLIENT_DATA_INSTANCE, keyMapper(key), Some(dataMapper(data)))
            case ServerBindingKeyInstance(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.SERVER_BINDING_KEY, keyMapper(key), None)
            case ServerReturnBindingKeyInstance(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.SERVER_RETURN_BINDING_KEY, keyMapper(key), None)
            case ServerExternalInstanceBinding(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.SERVER_EXTERNAL_INSTANCE_BINDING, keyMapper(key), None)
            case ServerPendingDataInstance(instanceNr, key) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.SERVER_PENDING_DATA_INSTANCE, keyMapper(key), None)
            case ServerFilledDataInstance(instanceNr, key, data) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.SERVER_FILLED_DATA_INSTANCE, keyMapper(key), Some(dataMapper(data)))
            case ServerDataInstance(instanceNr, key, data) => CanonicalProtocolInstanceProto(instanceNr, InstanceType.SERVER_DATA_INSTANCE, keyMapper(key), Some(dataMapper(data)))
          }
        }
        CanonicalRequestProto(idMapper(transactionId), protocalls.toVector, protoInstances.toVector, data.closeThread)
      }
    }
  }

  private def translateFromProto(proto: CanonicalRequestProto): ThreadMessage = ???
}

abstract sealed class Action[R <: Result]

abstract sealed class Result

case class ExceptionResult(exception: Throwable) extends Result

case class PingAction() extends Action[PingResult]

case class PingResult() extends Result

case class ProtocolMessageAction(var message: CanonicalRequestProto) extends Action[ProtocolMessageResult] {
  @throws(classOf[IOException])
  private def writeObject(out: ObjectOutputStream): Unit = message.writeDelimitedTo(out)

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in: ObjectInputStream): Unit = {
    CanonicalRequestProto.defaultInstance.mergeDelimitedFromStream(in).map {
      proto => this.message = proto
    }
  }
}

case class ProtocolMessageResult(var message: CanonicalRequestProto) extends Result {
  @throws(classOf[IOException])
  private def writeObject(out: ObjectOutputStream): Unit = message.writeDelimitedTo(out)

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in: ObjectInputStream): Unit = {
    CanonicalRequestProto.defaultInstance.mergeDelimitedFromStream(in).map {
      proto => this.message = proto
    }
  }
}

case class OpenTransactionAction(url: ServiceInstanceURL) extends Action[OpenTransactionResult]

case class OpenTransactionResult(id: LongLongID) extends Result

private case class Request(a: Action[_ <: Result], id: LongLongID)

private case class Response(r: Result, id: LongLongID)


private class Connection(private val channel: Channel) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val perThreadIdGenerator: ThreadLocal[LongLongIdGenerator] = new ThreadLocal[LongLongIdGenerator] {
    override def initialValue(): LongLongIdGenerator = LongLongIdGenerator.createGenerator()
  }

  private val handler: NettyClientHandler = channel.pipeline().get(classOf[NettyClientHandler])

  def send[A <: Action[R], R <: Result](action: A): Future[R] = {
    val p = Promise[Response]
    Future {
      val id = perThreadIdGenerator.get().getNextID
      logger.debug("generated ID {}", id)
      handler.resultsMap.put(id, p)
      id
    } flatMap {
      id => {
        logger.debug("write to channel")
        channel.write(Request(action, id))
      }
    } onFailure {
      case t => p failure (t)
    }
    p.future.map {
      res => {
        logger.debug("map response")
        res.r.asInstanceOf[R]
      }
    }
  }
}

private class NettyClientHandler extends ChannelInboundHandlerAdapter {

  val resultsMap = new ConcurrentHashMap[LongLongID, Promise[Response]]()

  val logger = LoggerFactory.getLogger(this.getClass)

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    logger.debug("client receive ")
    val resultMsgs = msgs.cast[Response]()
    import scala.collection.JavaConversions._
    resultMsgs.foreach(m => receiveMsg(ctx, m))
    msgs.releaseAllAndRecycle()
  }


  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.error("client error", cause)
    super.exceptionCaught(ctx, cause)
  }

  private def receiveMsg(ctx: ChannelHandlerContext, message: Response) {
    val toMerge = resultsMap.remove(message.id)
    assert(toMerge != null)
    message.r match {
      case ExceptionResult(e) => toMerge failure e
      case _ => toMerge success message
    }
  }
}

private class NettyServerHandler(val transactionApi: SamInjectionTransaction) extends ChannelInboundHandlerAdapter {
  val logger = LoggerFactory.getLogger(this.getClass)

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    val requestMsgs = msgs.cast[Request]()
    import scala.collection.JavaConversions._
    requestMsgs.foreach(m => handleSingleMessage(ctx, m))
    msgs.releaseAllAndRecycle()
  }


  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.error("exception on server", cause)
    super.exceptionCaught(ctx, cause)
  }

  private def handleSingleMessage(ctx: ChannelHandlerContext, msg: Request) {
    val action = msg.a
    try {
      action match {
        case PingAction() => ctx.write(Response(PingResult(), msg.id))
        case ProtocolMessageAction(canonicalMessage) => {
          val tid = mapID(canonicalMessage.`transactionID`)
          ???
        }
        case OpenTransactionAction(url) => {
          val tid = transactionApi.openTransactionContext(url)
          tid.flatMap {
            id => ctx.write(Response(OpenTransactionResult(id), msg.id))
          } recover {
            case e: Throwable => ctx.write(Response(ExceptionResult(e), msg.id))
          }
        }
      }
    } catch {
      case e: Throwable => {
        ctx.write(Response(ExceptionResult(e), msg.id))
      }
    }
  }

  private def mapID(idProto: LongLongIDProto): LongLongID = new LongLongID(idProto.`mask`, idProto.`linear`)

}
