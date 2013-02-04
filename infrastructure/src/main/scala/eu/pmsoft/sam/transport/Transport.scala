package eu.pmsoft.sam.transport

import scala.concurrent.{Promise, Future}
import io.netty.channel.{ChannelPipeline, ChannelHandlerContext, ChannelInboundHandlerAdapter, Channel}
import com.google.protobuf.MessageLite
import io.netty.handler.codec.protobuf.{ProtobufEncoder, ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
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





