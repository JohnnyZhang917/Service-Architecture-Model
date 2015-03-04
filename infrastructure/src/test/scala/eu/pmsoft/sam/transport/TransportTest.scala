/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.sam.transport

import org.testng.annotations.Test
import java.net.InetSocketAddress
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import io.netty.channel.ChannelPipeline
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import eu.pmsoft.sam.model.LongLongIDProto
import scala.collection.parallel.immutable.ParRange

class TransportTest {

  val logger = LoggerFactory.getLogger(this.getClass)

  private def serializableCodec[I, O](withDebug: Boolean = false): TransportCodec[I, O] = new TransportCodec[I, O] {
    def pipelineSetup(pipeline: ChannelPipeline) {
      if (withDebug) pipeline.addLast(new LoggingHandler(LogLevel.INFO))

      pipeline.addLast(
        new ObjectEncoder(),
        new ObjectDecoder(ClassResolvers.cacheDisabled(null))
      )
    }
  }

  val longLongIdCodec = TransportCodecBuilder.clientProtobuff[LongLongIDProto, LongLongIDProto](LongLongIDProto.getDefaultInstance())

  val longLongIdCodecDebug = TransportCodecBuilder.withDebug(longLongIdCodec)


  @Test def testProtobuffCodec() {
    val serverAddress = new InetSocketAddress(9016)
    val serverTransport = Promise[Transport[LongLongIDProto, LongLongIDProto]]
    def onConnection(transport: Transport[LongLongIDProto, LongLongIDProto]) {
      serverTransport.success(transport)
    }

    val server = ServerConnector[LongLongIDProto, LongLongIDProto](serverAddress, longLongIdCodec, onConnection)

    val client = ClientConnector[LongLongIDProto, LongLongIDProto](longLongIdCodecDebug)

    val fDispatcher = server.bind flatMap {
      done => client.connect(serverAddress)
    } map {
      ct => RPCDispatcher.clientDispatcher[LongLongIDProto, LongLongIDProto](ct)
    }

    val st = Await.result(serverTransport.future, 1 second)
    val dispatcher = Await.result(fDispatcher, 1 second)

    val send = LongLongIDProto.apply(0L, 1L)
    val response = LongLongIDProto.apply(1L, 2L)

    val fStr = dispatcher.dispatch(send)
    val serverResponse = for {
      rdone <- st.read() if rdone.`mask` == 0L && rdone.`linear` == 1L
      wdone <- st.write(response)
    } yield ()

    Await.result(serverResponse, 1 second)
    val result = Await.result(fStr, 1 second)
    assert(result == response)
  }

  @Test def testSerialCalls() {
    val nrOfMsg = 40
    def handler(msg: Int): Int = {
      msg + nrOfMsg
    }

    val codec = serializableCodec[Int, Int](false)
    val dispatcher = setup[Int, Int](new InetSocketAddress(9015), handler, codec)
    val seqRes = ParRange(1, 100, 1, true) map {
      iter =>
        val test = dispatcher flatMap {
          d => {
            val calls = (1 to nrOfMsg) map {
              d.dispatch(_)
            }
            Future.sequence(calls)
          }
        } map {
          seq => {
            val expected = (1 + nrOfMsg) to (nrOfMsg + nrOfMsg)
            assert(expected.size == seq.size)
            val bad = (seq zip expected).filter {
              case (s: Int, e: Int) => s != e
            }
            bad
          }
        }

        val result = Await.result(test, 10 second)
        result
    }
    val badSeq = seqRes filterNot {
      seq => seq.isEmpty
    }
    val allOk = seqRes forall {
      res => res.isEmpty
    }
    assert(allOk, "errors" + badSeq)
  }

  private def setup[I, O](address: InetSocketAddress, handler: I => O, codec: TransportCodec[I, O]): Future[RPCDispatcher[I, O]] = {
    def handlerFut(msg: I): Future[O] = Future {
      handler(msg)
    }
    def onConnection(transport: Transport[O, I]) {
      new SerialServerDispatcher[I, O](transport, handlerFut)
    }
    val server = ServerConnector[I, O](address, codec, onConnection)
    val client = ClientConnector[I, O](codec)

    server.bind flatMap {
      done => client.connect(address)
    } map {
      ct => RPCDispatcher.clientDispatcher[I, O](ct)
    }
  }

  @Test def testServerDispatcher() {

    val serverAddress = new InetSocketAddress(9014)
    val serverTransport = Promise[Transport[String, String]]

    def handler(msg: String): Future[String] = Future {
      msg match {
        case "_PING" => "_PONG"
        case _ => "ERROR"
      }
    }

    def onConnection(transport: Transport[String, String]) {
      new SerialServerDispatcher[String, String](transport, handler)
      serverTransport.success(transport)
    }

    val codec = serializableCodec[String, String]()

    val server = ServerConnector[String, String](serverAddress, codec, onConnection)

    val client = ClientConnector[String, String](codec)

    val fDispatcher = server.bind flatMap {
      done => client.connect(serverAddress)
    } map {
      ct => RPCDispatcher.clientDispatcher[String, String](ct)
    }

    Await.ready(serverTransport.future, 1 second)
    val dispatcher = Await.result(fDispatcher, 1 second)

    val fStr = dispatcher.dispatch("_PING")
    val result = Await.result(fStr, 1 second)
    assert(result == "_PONG")
  }


  @Test def testDispatcher() {
    val serverAddress = new InetSocketAddress(9013)
    val serverTransport = Promise[Transport[String, String]]
    def onConnection(transport: Transport[String, String]) {
      serverTransport.success(transport)
    }

    val codec = serializableCodec[String, String]()
    val server = ServerConnector[String, String](serverAddress, codec, onConnection)

    val client = ClientConnector[String, String](codec)

    val fDispatcher = server.bind flatMap {
      done => client.connect(serverAddress)
    } map {
      ct => RPCDispatcher.clientDispatcher[String, String](ct)
    }

    val st = Await.result(serverTransport.future, 1 second)
    val dispatcher = Await.result(fDispatcher, 1 second)

    val fStr = dispatcher.dispatch("PING")
    val serverResponse = for {
      rdone <- st.read() if rdone == "PING"
      wdone <- st.write("PONG")
    } yield ()

    Await.result(serverResponse, 1 second)
    val result = Await.result(fStr, 1 second)
    assert(result == "PONG")

  }

  @Test def testServerClientTransport() {
    val serverAddress = new InetSocketAddress(9012)
    val serverTransport = Promise[Transport[String, String]]
    def onConnection(transport: Transport[String, String]) {
      serverTransport.success(transport)
    }

    val codec = serializableCodec[String, String]()
    val server = ServerConnector[String, String](serverAddress, codec, onConnection)

    val client = ClientConnector[String, String](codec)

    val futureServerChannel = server.bind
    val futureClientTransport = futureServerChannel flatMap {
      serverCh => {
        client.connect(serverAddress)
      }
    }
    val transportConnected = futureClientTransport flatMap {
      clientDone => serverTransport.future
    }

    val st = Await.result(transportConnected, 1 second)
    val ct = Await.result(futureClientTransport, 0 second)

    logger.debug("client write")
    val testTransport = ct.write("Ping") flatMap {
      writeDone => {
        logger.debug("client write done")
        logger.debug("server read")
        st.read()
      }
    } flatMap {
      onServer => {
        logger.debug("server check")
        assert(onServer == "Ping")
        logger.debug("server write")
        st.write("Pong")
      }
    } flatMap {
      writeDone => {
        logger.debug("server write done")
        logger.debug("client read")
        ct.read()
      }
    } map {
      onClient => {
        logger.debug("client read done, final check")
        onClient == "Pong"
      }
    }
    assert(Await.result(testTransport, 1 second))
  }
}