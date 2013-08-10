package eu.pmsoft.sam.transport

import scala.util._
import scala.concurrent.{Future, Promise}
import java.util.concurrent.CancellationException
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import org.slf4j.LoggerFactory


object NettyTransport {

}

object NettyFutureTranslation {
  val logger = LoggerFactory.getLogger(this.getClass)

  private def futureFromNetty[T](channelFuture: ChannelFuture, map: Channel => T): Future[T] = {
    val promise = Promise[T]()
    channelFuture.addListener(new ChannelFutureListener {
      def operationComplete(p1: ChannelFuture) {
        promise complete Try(
          if (p1.isSuccess) map(p1.channel())
          else if (p1.isCancelled) throw new CancellationException
          else throw p1.cause()
        )
      }
    })
    promise.future
  }

  implicit def futureChannel(cf: ChannelFuture): Future[Channel] = futureFromNetty(cf, identity _)

  implicit def futureUnit(cf: ChannelFuture): Future[Unit] = futureFromNetty(cf, _ => ())
}

private object NettyUtils {
  lazy val bossGroup: NioEventLoopGroup = new NioEventLoopGroup()
  lazy val workerGroup: NioEventLoopGroup = new NioEventLoopGroup()
  lazy val clientGroup: NioEventLoopGroup = new NioEventLoopGroup()
}
