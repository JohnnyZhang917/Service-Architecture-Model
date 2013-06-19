package eu.pmsoft.sam.see

import org.testng.annotations.Test
import io.netty.bootstrap.{Bootstrap, ServerBootstrap}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.{NioSocketChannel, NioServerSocketChannel}
import io.netty.channel._
import io.netty.handler.logging.{LoggingHandler, LogLevel}
import io.netty.channel.socket.SocketChannel
import io.netty.buffer.{Unpooled, ByteBuf}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.slf4j.{LoggerFactory, Logger}


class NettyFutureApiTest {

  val logger = LoggerFactory.getLogger(this.getClass)

  @Test
  def testClientServerCommunication() {
    val bossGroup: NioEventLoopGroup = new NioEventLoopGroup()
    val workerGroup: NioEventLoopGroup = new NioEventLoopGroup()

    val serverChannel = Future {
      createServer(9001, bossGroup, workerGroup)
    }

    serverChannel map {
      channel => channel.closeFuture().addListener(new ChannelFutureListener {
        def operationComplete(future: ChannelFuture) {
          bossGroup.shutdownGracefully()
          workerGroup.shutdownGracefully()
        }
      })
    }

    val clientGroup: NioEventLoopGroup = new NioEventLoopGroup()
    val clientChannel = Future {
      createClient(clientGroup)
    }
  }

  def createClient(group: EventLoopGroup) {
    val b = new Bootstrap()
    b.group(group)
      .channel(classOf[NioSocketChannel])
      .option(ChannelOption.TCP_NODELAY, java.lang.Boolean.valueOf(true))
      .handler(new ChannelInitializer[SocketChannel] {
      def initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(
          new LoggingHandler(LogLevel.INFO),
          new TestEchoClientHandler(10)
        )
      }
    })
  }

  def createServer(port: Int, bossGroup: NioEventLoopGroup, workerGroup: NioEventLoopGroup) = {
    val server = new ServerBootstrap()

    server
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .option(ChannelOption.SO_BACKLOG, java.lang.Integer.valueOf(100))
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel] {
      def initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(
          new LoggingHandler(LogLevel.INFO),
          new TestEchoServerHandler()
        )
      }
    })

    server.bind(port).syncUninterruptibly().channel()
  }
}


class TestEchoServerHandler extends ChannelInboundByteHandlerAdapter {
  val logger = LoggerFactory.getLogger(this.getClass)
  def inboundBufferUpdated(ctx: ChannelHandlerContext, in: ByteBuf) {
    logger.debug("server in " + in)
    val out = ctx.nextOutboundByteBuffer()
    out.writeBytes(in)
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    ctx.close()
  }
}

class TestEchoClientHandler(firstMessageSize: Int) extends ChannelInboundByteHandlerAdapter {

  val logger = LoggerFactory.getLogger(this.getClass)
  val firstMessage: ByteBuf = Unpooled.buffer(firstMessageSize)

  (0 to firstMessage.capacity()).foreach {
    i => firstMessage.writeByte(i.toByte)
  }

  def inboundBufferUpdated(ctx: ChannelHandlerContext, in: ByteBuf) {
    logger.debug("client in " + in)
    val out = ctx.nextOutboundByteBuffer()
    out.writeBytes(in)
    ctx.flush()
  }

  override def channelActive(ctx: ChannelHandlerContext) {
    ctx.write(firstMessage)
  }


}