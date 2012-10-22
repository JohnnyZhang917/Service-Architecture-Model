package pmsoft.sam.protocol.execution.serial;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;

public class ClientCall {

	private final String host;
	private final int port;
	private ClientCallHandler handler;

	public ClientCall(String host, int port, CanonicalProtocolRequest request) {
		super();
		this.host = host;
		this.port = port;
		this.handler = new ClientCallHandler(request);
	}

	public CanonicalProtocolRequestData run() throws Exception {
		Bootstrap b = new Bootstrap();
		try {
			b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class).remoteAddress(host, port).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {

					ch.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), handler);
				}
			});

			// Start the connection attempt.
			b.connect().sync().channel().closeFuture().sync();
		} finally {
			b.shutdown();
		}
		return handler.getResponce();
	}

}
