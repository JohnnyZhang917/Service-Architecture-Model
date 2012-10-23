package pmsoft.sam.protocol.execution.serial;

import pmsoft.sam.protocol.execution.ServiceExecutionEnvironment;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

class SerializableServer implements Runnable {

	private final Integer port;
	private final SerializableServerHandler hander;
	private ServerBootstrap b;
	private ChannelFuture channel;

	@Inject
	public SerializableServer(@Named(ServiceExecutionEnvironment.SERVICE_PORT_NAMED_BINDING) Integer port, SerializableServerHandler hander) {
		this.hander = hander;
		this.port = port;
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
		b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
		.channel(NioServerSocketChannel.class)
		.localAddress(port)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline()
						.addLast(new LoggingHandler(LogLevel.TRACE))
						.addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), hander);
					}
				});
		channel = b.bind().sync();
	}

	void shutdown() {
		channel.awaitUninterruptibly();
		b.shutdown();
	}

}