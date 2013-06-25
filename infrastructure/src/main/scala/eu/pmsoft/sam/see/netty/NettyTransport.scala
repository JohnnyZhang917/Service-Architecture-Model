package eu.pmsoft.sam.see.netty

import eu.pmsoft.sam.see.{ThreadMessage, EnvironmentConnection, CanonicalProtocolCommunicationApi}
import scala.concurrent.{ExecutionContext, Future, Promise}
import io.netty.channel._
import scala.util.{Success, Failure, Try}
import ExecutionContext.Implicits.global
import java.util.concurrent.{ConcurrentHashMap, CancellationException}
import io.netty.bootstrap.{ServerBootstrap, Bootstrap}
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioSocketChannel}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.channel.nio.NioEventLoopGroup
import eu.pmsoft.sam.see.netty.NettyFutureTranslation._
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.idgenerator.{LongLongID, LongLongIdGenerator}
import scala.collection.mutable
import java.net.InetSocketAddress
import java.io.{ObjectOutputStream, ByteArrayOutputStream}

object NettyTransport {

  def createNettyServer(address: InetSocketAddress) = new NettyCanonicalProtocolServer(address)

  def api(): CanonicalProtocolCommunicationApi = new NettyCanonicalProtocolCommunicationApi()

}


class NettyCanonicalProtocolServer(address: InetSocketAddress) {
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
        new NettyServerHandler()
      )
    }
  })

  val channel : Future[Channel] = server.bind(address)

  channel.flatMap {
    c => c.closeFuture()
  } onComplete {
    _ match {
      case Failure(exception) => logger.error("exception",exception)
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
  val connectionMap : mutable.Map[String,EnvironmentConnection] = mutable.Map.empty
  def getEnvironmentConnection(host: String, port: Int): EnvironmentConnection = {
    val key = host + port
    connectionMap.getOrElse(key, {
      val connection = new NettyEnvironmentConnection(host, port)
      connectionMap.put(key,connection)
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
    c => c.send[PingAction,PingResult](PingAction())
  }

  def protocolAction(message: ThreadMessage): Future[ThreadMessage] = connection.flatMap {
    c => c.send[ProtocolMessageAction,ProtocolMessageResult](ProtocolMessageAction(message))
  }.map {
    response => response.message
  }
}

abstract sealed class Action[R <: Result]

abstract sealed class Result

case class PingAction() extends Action[PingResult]

case class PingResult() extends Result

case class ProtocolMessageAction(message : ThreadMessage) extends Action[ProtocolMessageResult]
case class ProtocolMessageResult(message : ThreadMessage) extends Result

private case class Request(a : Action[_ <: Result], id : LongLongID)
private case class Response(r : Result, id : LongLongID)


private class Connection(private val channel: Channel) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val perThreadIdGenerator : ThreadLocal[LongLongIdGenerator] = new ThreadLocal[LongLongIdGenerator]{
    override def initialValue(): LongLongIdGenerator = LongLongIdGenerator.createGenerator()
  }

  private val handler: NettyClientHandler = channel.pipeline().get(classOf[NettyClientHandler])

  def send[A <: Action[R], R <: Result](action: A): Future[R] = {
    val p = Promise[Response]
    Future {
      val id = perThreadIdGenerator.get().getNextID
      logger.debug("generated ID {}", id)
      handler.resultsMap.put(id,p)
      id
    } flatMap {
      id => {
        logger.debug("write to channel")
        channel.write(Request(action,id))
      }
    } onFailure {
      case t => p failure(t)
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

  val resultsMap = new ConcurrentHashMap[LongLongID,Promise[Response]]()

  val logger = LoggerFactory.getLogger(this.getClass)

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    logger.debug("client receive ")
    val resultMsgs = msgs.cast[Response]()
    import scala.collection.JavaConversions._
    resultMsgs.foreach( m => receiveMsg(ctx,m))
    msgs.releaseAllAndRecycle()
  }


  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.error("client error", cause)
    super.exceptionCaught(ctx, cause)
  }

  private def receiveMsg(ctx: ChannelHandlerContext,message: Response) {
    val toMerge = resultsMap.remove(message.id)
    assert(toMerge != null)
    toMerge success message
  }
}
private class NettyServerHandler extends ChannelInboundHandlerAdapter {
  val logger = LoggerFactory.getLogger(this.getClass)

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    val requestMsgs = msgs.cast[Request]()
    import scala.collection.JavaConversions._
    logger.debug("server receive ")
    requestMsgs.foreach( m => handleSingleMessage(ctx,m))
    msgs.releaseAllAndRecycle()
  }


  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.error("exception on server", cause)
    super.exceptionCaught(ctx, cause)
  }

  private def handleSingleMessage(ctx: ChannelHandlerContext,msg : Request) {
    val action = msg.a
    action match {
      case PingAction() => ctx.write(Response(PingResult(),msg.id))
      case ProtocolMessageAction(canonicalMessage) => ???
    }
  }

}


