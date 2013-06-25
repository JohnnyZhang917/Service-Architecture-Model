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
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.see.netty.{PingResult, NettyTransport, NettyFutureTranslation}
import eu.pmsoft.sam.see.netty.NettyFutureTranslation._
import java.net.InetSocketAddress


class NettyFutureApiTest {

  val logger = LoggerFactory.getLogger(this.getClass)

  @Test
  def testActionResultHandling() {
    val clientApi = NettyTransport.api()
    val address = new InetSocketAddress(9002)
    val server = NettyTransport.createNettyServer(address)

    val client = server.channel.map {
      serverReady => clientApi.getEnvironmentConnection("localhost",9002)
    }

    val testResult = client.flatMap {
      c => c.ping
    }.map {
      ping => ping match {
        case PingResult() => true
        case _ => false
      }
    }

    assert(Await.result(testResult,3 seconds))
  }


  @Test
  def testClientServerCommunication() {
    val bossGroup: NioEventLoopGroup = new NioEventLoopGroup()
    val workerGroup: NioEventLoopGroup = new NioEventLoopGroup()
    val clientGroup: NioEventLoopGroup = new NioEventLoopGroup()
    val port = 9001
    val address = new InetSocketAddress(port)
    val serverChannelFuture = createServer(address , bossGroup, workerGroup)
    val closeServerFuture = serverChannelFuture.flatMap {
      channel => channel.closeFuture()
    }
    closeServerFuture.map {
      closed => {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
      }
    }

    val clientChannelFuture = serverChannelFuture.flatMap {
      serverReady => {
        createClient(clientGroup,port)
      }
    }
    val closeClientFuture = clientChannelFuture.flatMap {
      client => client.closeFuture()
    }
    closeClientFuture.flatMap {
      clientDone => {
        serverChannelFuture.flatMap {
          server => server.disconnect()
        }
      }
    }
    closeClientFuture.map {
      closed => {
        clientGroup.shutdownGracefully()
      }
    }
    val allClosed = for {
      serverClosed <- closeServerFuture
      clientClosed <- closeClientFuture
    } yield ()

    Await.result(allClosed, 30 seconds)
  }

  def createClient(group: EventLoopGroup, connectTo : Int) = {
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

    NettyFutureTranslation.futureFromNetty(b.connect("localhost",connectTo))
  }

  def createServer(port: InetSocketAddress, bossGroup: NioEventLoopGroup, workerGroup: NioEventLoopGroup) =  {
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
    NettyFutureTranslation.futureFromNetty(server.bind(port))
  }



}


class TestEchoServerHandler extends ChannelInboundHandlerAdapter {
  val logger = LoggerFactory.getLogger(this.getClass)

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    logger.debug("server receive ")
    ctx.write(msgs)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.debug("server exception {}", cause)
    ctx.close()
  }
}

class TestEchoClientHandler(firstMessageSize: Int) extends ChannelInboundHandlerAdapter {

  val logger = LoggerFactory.getLogger(this.getClass)
  val firstMessage: ByteBuf = Unpooled.buffer(firstMessageSize)
  var counter = 3

  (0 to firstMessage.capacity()).foreach {
    i => firstMessage.writeByte(i.toByte)
  }

  override def channelActive(ctx: ChannelHandlerContext) {
    logger.debug("client active")
    ctx.write(firstMessage)
  }

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    logger.debug("debug received ")
    counter = counter - 1
    if( counter < 0) {
      ctx.close()
    } else {
      ctx.write(msgs)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.debug("debug execption {}", cause)
    ctx.close()
  }
}