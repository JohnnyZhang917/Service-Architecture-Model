package eu.pmsoft.execution;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;

import java.net.URL;
import java.util.UUID;

class ThreadClientConnection {

    private final Channel channel;

    @InjectLogger
    private Logger logger;

    private final ClientConnectionHandler clientConnectionHandler;

    private final ThreadExecutionModelFactory model;

    @Inject
    ThreadClientConnection(@Assisted final URL address, ClientConnectionHandler handler, ThreadExecutionModelFactory model) {
        this.clientConnectionHandler = handler;
        this.model = model;
        Bootstrap client = new Bootstrap();
        client.group(new NioEventLoopGroup()).channel(NioSocketChannel.class).remoteAddress(address.getHost(), address.getPort()).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new LoggingHandler(LogLevel.TRACE))
                        .addLast(new ReadTimeoutHandler(3000))
                        .addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), clientConnectionHandler);
            }
        });
        //TODO async initialization of client connections
        this.channel = client.connect().syncUninterruptibly().channel();

    }

//    ThreadMessagePipe bindPipe(String signature, UUID transactionID, URL address) {
//        Preconditions.checkNotNull(signature);
//        Preconditions.checkNotNull(transactionID);
//        ThreadMessagePipe pipe = model.createPipe(channel, signature, transactionID, address);
//        clientConnectionHandler.bindPipe(signature, pipe);
//        return pipe;
//    }

}
