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

import io.netty.channel._
import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.channel.socket.SocketChannel
import scala.concurrent.Future

object ServerConnector {
  def apply[Req, Res](address: InetSocketAddress, codec: TransportCodec[Req, Res], dispatcher: Transport[Res, Req] => Unit): ServerConnector[Req, Res] = new NettyRpcServer[Req, Res](address, codec, dispatcher)
}

trait ServerConnector[Req, Res] {
  def bind: Future[Channel]
}

private class NettyRpcServer[Req, Res](address: InetSocketAddress, codec: TransportCodec[Req, Res], serving: Transport[Res, Req] => Unit) extends ServerConnector[Req, Res] {

  import NettyFutureTranslation._

  val logger = LoggerFactory.getLogger(this.getClass)
  private[this] val server = new ServerBootstrap()

  server
    .group(NettyUtils.bossGroup, NettyUtils.workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .option(ChannelOption.SO_BACKLOG, java.lang.Integer.valueOf(100))
    .option(ChannelOption.TCP_NODELAY, java.lang.Boolean.valueOf(true))
    .option(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.valueOf(true))
    .handler(new LoggingHandler(LogLevel.INFO))
    .childHandler(new TransportServerBridge(codec, serving))

  lazy val channel: Future[Channel] = server.bind(address)

  def bind: Future[Channel] = channel
}

private class TransportServerBridge[Req, Res](codec: TransportCodec[Req, Res], serving: Transport[Res, Req] => Unit) extends ChannelInitializer[SocketChannel] {
  def initChannel(ch: SocketChannel) {
    codec.pipelineSetup(ch.pipeline())
    val transport = new ChannelTransport[Res, Req](ch)
    ch.pipeline().addLast("transport", transport)
    serving(transport)
  }
}
