package eu.pmsoft.sam.see

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import io.netty.channel._
import java.net.InetSocketAddress
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.timeout.{ReadTimeoutHandler, WriteTimeoutHandler}
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model.{ExternalServiceBind, ServiceConfigurationID, ServiceInstanceURL}
import scala.collection.mutable

object CanonicalTransportLayer {

  def apply(port: Int) = new CanonicalTransportServer(port)
}

trait SlotExecutionPipe {
  def sendProtocolExecution(message: CanonicalProtocolMessage): Future[CanonicalProtocolMessage]
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

private class TMPSlotExecutionPipe extends SlotExecutionPipe {
  def sendProtocolExecution(message: CanonicalProtocolMessage): Future[CanonicalProtocolMessage] = ???
}

private class DirectExecutionPipe(val executor : CanonicalProtocolExecutor ) extends SlotExecutionPipe {

  def sendProtocolExecution(message: CanonicalProtocolMessage): Future[CanonicalProtocolMessage] = {
    Future {
      executor.execute(message)
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
