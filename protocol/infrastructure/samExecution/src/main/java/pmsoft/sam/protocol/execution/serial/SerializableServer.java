package pmsoft.sam.protocol.execution.serial;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

class SerializableServer implements Runnable {

	private Integer port;
	private final SerializableServerHandler hander;
	private ServerBootstrap b;

	public int getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Inject
	public SerializableServer(SerializableServerHandler hander) {
		this.hander = hander;
	}

	@Override
	public void run() {
		Preconditions.checkState(port != null);
		try {
			runServer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void runServer() throws Exception {
		b = new ServerBootstrap();
		try {
			b.group(new NioEventLoopGroup(), new NioEventLoopGroup()).channel(NioServerSocketChannel.class).localAddress(port)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), hander);
						}
					});

			// Bind and start to accept incoming connections.
			b.bind().sync().channel().closeFuture().sync();
		} finally {
			b.shutdown();
		}
	}

	void shutdown() {
		b.shutdown();
	}

}