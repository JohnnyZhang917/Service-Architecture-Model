package eu.pmsoft.sam.see.transport.nettyrefactor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import eu.pmsoft.execution.ThreadMessage;
import eu.pmsoft.see.api.model.SIURL;
import eu.pmsoft.see.api.model.STID;
import eu.pmsoft.sam.see.transport.SamTransportChannel;
import eu.pmsoft.sam.see.transport.SamTransportCommunicationContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class SamTransportServerNetty implements SamTransportCommunicationContext {

    private final BiMap<STID, SIURL> exposedServices = HashBiMap.create();

    private final Provider<SamTransportServerConnectionHandler> serverHandlerProvider;
    private final Provider<SamTransportClientConnectionHandler> clientHandlerProvider;
    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private InetSocketAddress serverAddress;

    @Inject
    public SamTransportServerNetty(Provider<SamTransportServerConnectionHandler> serverHandlerProvider, Provider<SamTransportClientConnectionHandler> clientHandlerProvider) {
        this.serverHandlerProvider = serverHandlerProvider;
        this.clientHandlerProvider = clientHandlerProvider;
    }

    public void setupServer(int port) {
        serverAddress = new InetSocketAddress(port);
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup()).channel(NioServerSocketChannel.class).localAddress(serverAddress)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE)).addLast(new WriteTimeoutHandler(3000)).addLast(new ReadTimeoutHandler(3000))
                                .addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), serverHandlerProvider.get());
                    }
                });
        //TODO async server start
        serverBootstrap.bind().syncUninterruptibly();
    }

    @Override
    public SIURL liftServiceTransaction(STID stid) {
        assert serverAddress != null;
        if (!exposedServices.containsKey(stid)) {
            exposedServices.put(stid,createUrl(stid));
        }
        return exposedServices.get(stid);
    }

    @Override
    public List<SamTransportChannel> createClientConnectionChannels(UUID transactionID, List<URL> endpointAddressList) {
        ImmutableList.Builder<SamTransportChannel> builder = ImmutableList.builder();
        for (URL externalServiceEndpoint : endpointAddressList) {
            builder.add(createClientChannel(transactionID,externalServiceEndpoint));
        }
        return builder.build();
    }

    private SamTransportChannel createClientChannel(UUID transactionID, URL externalServiceEndpoint) {
        String host = externalServiceEndpoint.getHost();
        int port = externalServiceEndpoint.getPort();
        SamClientConnectionPool pool = getServerPool(host,port);
        return pool.bindChannel(transactionID, externalServiceEndpoint);
    }

    ConcurrentMap<String,SamClientConnectionPool> externalClientPools = Maps.newConcurrentMap();
    private SamClientConnectionPool getServerPool(String host, int port) {
        assert host != null;
        String endpointKey = host + port;
        if( ! externalClientPools.containsKey(endpointKey) ){
            externalClientPools.putIfAbsent(endpointKey,new SamClientConnectionPool(host,port));
        }
        return externalClientPools.get(endpointKey);
    }

    private SIURL createUrl(STID stid) {
        URL url = null;
        try {
            url = new URL("http",serverAddress.getHostName(),serverAddress.getPort(),"service/"+stid.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return SIURL.fromUrl(url);
    }

    private class SamClientConnectionPool {

        private final NioEventLoopGroup nioEventLoopGroup;
        private final InetSocketAddress serverAddress;

        public SamClientConnectionPool(String host, int port) {
            this.nioEventLoopGroup = new NioEventLoopGroup();
            this.serverAddress = new InetSocketAddress(host,port);
        }


        public SamTransportChannel bindChannel(UUID transactionID, URL path) {
            return new SamTransportChannelClientReference(transactionID,path);
        }

        private Channel useConnection(SamTransportChannelClientReference samTransportChannelClientReference) {
            Bootstrap client = new Bootstrap();
            client.group(nioEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .remoteAddress(serverAddress)
                    ;
            client.group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
//                    .remoteAddress(new InetSocketAddress(host,port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.TRACE))
                                    .addLast("idleStateHandler", new IdleStateHandler(60, 30, 0))
                                    .addLast(new ReadTimeoutHandler(3000))
                                    .addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), clientHandlerProvider.get())
                            ;
                        }
                    });
            // TODO pooling
            ChannelFuture connect = client.connect();


            return connect.syncUninterruptibly().channel();
        }

        private void releaseConnection(Channel channel) {
            channel.close();
        }

        private class SamTransportChannelClientReference implements SamTransportChannel {
            private final URL serviceEndpoint;
            private final UUID transactionID;
            private Channel channel;
            private final ConcurrentLinkedQueue<ThreadMessage> messageInputQueue = new ConcurrentLinkedQueue<ThreadMessage>();
            private final Object waitMonitor = new Object();

            public SamTransportChannelClientReference(UUID transactionID, URL path) {
                this.transactionID = transactionID;
                this.serviceEndpoint = path;
            }

            @Override
            public void bindConnection() {
                channel = SamClientConnectionPool.this.useConnection(this);
            }

            @Override
            public void unbindConnection() {
                SamClientConnectionPool.this.releaseConnection(channel);
            }

            @Override
            public void sendMessage(ThreadMessage message) {
                channel.write(message);
            }

            public void receiveMessage(ThreadMessage message) {
                //TODO FIXME 111  musze odpowiedzi z serwera przyjac i dodac tutaj
                messageInputQueue.add(message);
                synchronized (waitMonitor) {
                    waitMonitor.notify();
                }
            }

            @Override
            public ThreadMessage waitResponse() {
                ThreadMessage message = messageInputQueue.poll();
                if( message == null ) {
                    synchronized (waitMonitor) {
                        while (messageInputQueue.isEmpty()) {
                            try {
                                waitMonitor.wait();
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                    }
                }
                return messageInputQueue.poll();
            }

            @Override
            public ThreadMessage pollMessage() {
                return messageInputQueue.poll();
            }
        }

    }
}
