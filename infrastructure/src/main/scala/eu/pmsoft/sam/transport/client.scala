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

import java.net.InetSocketAddress
import scala.concurrent._
import ExecutionContext.Implicits.global
import io.netty.channel.socket.SocketChannel
import io.netty.channel.{Channel, ChannelOption, ChannelInitializer}
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.bootstrap.Bootstrap
import org.slf4j.LoggerFactory


object ClientConnector {
  def apply[In, Out](codec: TransportCodec[In, Out]): ClientConnector[In, Out] = new NettyRpcClient(codec)
}

trait ClientConnector[In, Out] {

  def connect(serverAddress: InetSocketAddress): Future[Transport[In, Out]]

}

private class NettyRpcClient[In, Out](codec: TransportCodec[In, Out]) extends ClientConnector[In, Out] {
  val logger = LoggerFactory.getLogger(this.getClass)

  import NettyFutureTranslation._

  def connect(serverAddress: InetSocketAddress): Future[Transport[In, Out]] = {
    val transportPromise = Promise[Transport[In, Out]]
    val b = new Bootstrap()
    b.group(NettyUtils.clientGroup)
      .channel(classOf[NioSocketChannel])
      .option(ChannelOption.TCP_NODELAY, java.lang.Boolean.valueOf(true))
      .option(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.valueOf(true))
      .handler(new NettyClientInitializer(transportPromise, codec))

    val connected: Future[Channel] = b.connect(serverAddress)
    connected flatMap {
      ch => transportPromise.future
    }
  }
}

private class NettyClientInitializer[In, Out](val promise: Promise[Transport[In, Out]], val codec: TransportCodec[In, Out]) extends ChannelInitializer[SocketChannel] {
  def initChannel(ch: SocketChannel) {
    codec.pipelineSetup(ch.pipeline())
    val transport = new ChannelTransport[In, Out](ch)
    ch.pipeline().addLast(transport)
    promise.success(transport)
  }
}

