package eu.pmsoft.execution;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public final class ThreadExecutionServer implements ServiceEndpointAddressProvider {

    private final ThreadExecutionManager executionService;
    private final ServerBootstrap serverBootstrap;
    private final Provider<ProviderConnectionHandler> providerServerConnectionHandler;
    private final InetSocketAddress serverAddress;

    @InjectLogger
    private Logger logger;

    @Inject
    ThreadExecutionServer(ThreadExecutionManager executionService, Provider<ProviderConnectionHandler> providerServerConnectionHandler, InetSocketAddress serverAddress) {
        super();
        this.executionService = executionService;
        this.serverAddress = serverAddress;
        this.serverBootstrap = new ServerBootstrap();
        this.providerServerConnectionHandler = providerServerConnectionHandler;
    }

    public void startServer() {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        logger.debug("starting local server on address {}", serverAddress);
        // TODO async bind to socket address
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup()).channel(NioServerSocketChannel.class).localAddress(serverAddress)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE)).addLast(new WriteTimeoutHandler(3000)).addLast(new ReadTimeoutHandler(3000))
                                .addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), providerServerConnectionHandler.get());
                    }
                });
        Channel channel = serverBootstrap.bind().syncUninterruptibly().channel();
        assert channel != null;
    }

    public void shutdownServer() {
        logger.debug("shutdown local server on address {}", serverAddress);
        serverBootstrap.shutdown();
    }

    public <R, T> Future<R> executeServiceAction(ServiceAction<R, T> serviceAction) {
        //TODO po co takie proste przekazanie
        return executionService.executeServiceAction(serviceAction);
    }

    @Override
    public String getServerEndpointBase() {
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }
}


