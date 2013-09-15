package eu.pmsoft.sam.transport


import scala.concurrent._
import ExecutionContext.Implicits.global

import io.netty.channel.{ChannelPipeline, ChannelHandlerContext, ChannelInboundHandlerAdapter, Channel}
import com.google.protobuf.MessageLite
import io.netty.handler.codec.protobuf.{ProtobufEncoder, ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentLinkedQueue
import scala.util.{Failure, Success}

trait Transport[In, Out] {

  def write(msg: In): Future[Unit]

  def read(): Future[Out]

  def isOpen: Boolean

  def onClose: Future[Unit]
}

object TransportCodecBuilder {
  def clientProtobuff[InProto <: MessageLite, OutProto <: MessageLite](prototype: OutProto): TransportCodec[InProto, OutProto] =
    new TransportCodec[InProto, OutProto] {
      def pipelineSetup(p: ChannelPipeline) {
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
        p.addLast("protobufDecoder", new ProtobufDecoder(prototype))
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
        p.addLast("protobufEncoder", new ProtobufEncoder)
      }
    }

  def serverProtobuff[InProto <: MessageLite, OutProto <: MessageLite](prototype: InProto): TransportCodec[InProto, OutProto] =
    new TransportCodec[InProto, OutProto] {
      def pipelineSetup(p: ChannelPipeline) {
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
        p.addLast("protobufDecoder", new ProtobufDecoder(prototype))
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
        p.addLast("protobufEncoder", new ProtobufEncoder)
      }
    }

  def withDebug[In, Out](codec: TransportCodec[In, Out]): TransportCodec[In, Out] = new TransportCodec[In, Out]() {
    def pipelineSetup(pipeline: ChannelPipeline) {
      pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO))
      codec.pipelineSetup(pipeline)
    }
  }
}

abstract class TransportCodec[In, Out] {

  def pipelineSetup(pipeline: ChannelPipeline): Unit

}


class ChannelTransport[In, Out](ch: Channel) extends ChannelInboundHandlerAdapter with Transport[In, Out] {

  import NettyFutureTranslation._

  private[this] val readq = new AsyncQueue[Out]
  private[this] val writeq = new ConcurrentLinkedQueue[(In, Promise[Unit])]()

  def write(msg: In): Future[Unit] = {
    val p = Promise[Unit]()
    writeq.offer((msg, p))
    rollWrite()
    p.future
  }

  private[this] val counter = new AtomicInteger(0)

  def rollWrite() {
    if (counter.getAndIncrement() == 0) {
      do {
        val toSend = writeq.poll()
        // TODO closed
        val fw: Future[Unit] = ch.writeAndFlush(toSend._1)
        fw.onComplete {
          case Success(()) => toSend._2.success(())
          case Failure(t) => toSend._2.failure(t)
        }
      } while (counter.decrementAndGet() > 0)
    }
  }

  def read(): Future[Out] = readq.poll()

  def isOpen: Boolean = ch.isOpen

  private[this] val closep = Promise[Unit]

  def onClose: Future[Unit] = closep.future

  override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val response = msg.asInstanceOf[Out]
    readq.offer(response)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    readq.fail(cause)
    ctx.close()
  }
}

