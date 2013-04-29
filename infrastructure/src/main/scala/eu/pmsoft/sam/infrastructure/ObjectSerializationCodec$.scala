package eu.pmsoft.sam.infrastructure

//import com.twitter.finagle.{Codec, CodecFactory}
//import org.jboss.netty.channel.{Channels, ChannelPipelineFactory}
//import org.jboss.netty.handler.codec.serialization.{ObjectDecoder, ClassResolvers, ObjectEncoder}
//import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}

//object ObjectSerializationCodec extends ObjectSerializationCodec
//
//class ObjectSerializationCodec extends CodecFactory[String, String] {
//  def server = Function.const {
//    new Codec[String, String] {
//      def pipelineFactory = new ChannelPipelineFactory {
//        def getPipeline = {
//          val pipeline = Channels.pipeline()
//                    pipeline.addLast("line",new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter: _*))
//          pipeline.addLast("objectDecoder", new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())))
//          pipeline.addLast("objectEncoder", new ObjectEncoder())
//          pipeline
//        }
//      }
//    }
//  }
//
//  def client = Function.const {
//    new Codec[String, String] {
//      def pipelineFactory = new ChannelPipelineFactory {
//        def getPipeline = {
//          val pipeline = Channels.pipeline()
//          pipeline.addLast("objectEncode", new ObjectEncoder())
//          pipeline.addLast("objectDecode", new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())))
//          pipeline
//        }
//      }
//    }
//  }
//}
