package pmsoft.execution;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import pmsoft.injectionUtils.logger.InjectLogger;

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


interface ModelFactory {

    ThreadExecutionContext threadExecutionContext(ExecutionContextInternalLogic internalLogic, ThreadMessagePipe headCommandPipe,
                                                  List<ThreadMessagePipe> endpoints,UUID transactionID);

    ThreadClientConnection openConnection(URL serverAddress);

    ThreadMessagePipe createPipe(Channel connection,String signature, UUID transactionID, URL address);

    ThreadExecutionServer server(InetSocketAddress serverAddress);
}

public final class ThreadExecutionServer {
    private final ThreadExecutionManager executionService;
    private final InetSocketAddress serverAddress;
    private final ServerBootstrap serverBootstrap;
    private final Provider<ProviderConnectionHandler> providerServerConnectionHandler;
    private Channel channel;

    @InjectLogger
    private Logger logger;

    @Inject
    ThreadExecutionServer(ThreadExecutionManager executionService, Provider<ProviderConnectionHandler> providerServerConnectionHandler, @Assisted InetSocketAddress serverAddress) {
        super();
        this.executionService = executionService;
        this.serverAddress = serverAddress;
        this.serverBootstrap = new ServerBootstrap();
        this.providerServerConnectionHandler = providerServerConnectionHandler;
    }

    public void startServer() {
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
        channel = serverBootstrap.bind().syncUninterruptibly().channel();
    }

    public void shutdownServer() {
        logger.debug("shutdown local server on address {}", serverAddress);
        serverBootstrap.shutdown();
    }

    public <R, T> Future<R> executeServiceAction(ServiceAction<R, T> serviceAction) {
        return executionService.executeServiceAction(serviceAction);
    }

}

class ProviderConnectionHandler extends ChannelInboundMessageHandlerAdapter<ThreadMessage> {

    @InjectLogger
    private Logger logger;
    private final ExecutionContextManager contextManager;
    private final ThreadExecutionManager executionManager;
    // TODO make something more efficient to multiplex the connection
    private Map<String,ThreadExecutionContext> transactionBinding = Maps.newHashMap();

    @Inject
    ProviderConnectionHandler(ExecutionContextManager contextManager, ThreadExecutionManager executionManager) {
        this.contextManager = contextManager;
        this.executionManager = executionManager;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, ThreadMessage msg) throws Exception {
        try {
            ThreadMessage.ThreadProtocolMessageType messageType = msg.getMessageType();
            switch (messageType) {
                case INITIALIZE_TRANSACTION:
                    initializeTransaction(ctx,msg);
                    break;
                case CLOSE_TRANSACTION:
                    closeTransaction(ctx, msg);
                    break;
                case CANONICAL_PROTOCOL_EXECUTION:
                    execute(msg);
                    break;
            }
        } catch (Exception anyException) {
            ThreadMessage exception = new ThreadMessage();
            exception.setMessageType(ThreadMessage.ThreadProtocolMessageType.EXCEPTION_MESSAGE);
            exception.setPayload(anyException);
            ctx.write(exception);
            throw anyException;
        }
    }

    private void execute(ThreadMessage msg) {
        Preconditions.checkState(transactionBinding.containsKey(msg.getSignature()));
        ThreadExecutionContext context = transactionBinding.get(msg.getSignature());
        ThreadMessagePipe head = context.getHeadCommandPipe();
        head.receiveMessage(msg);
        if (!context.isExecutionRunning()) {
            executionManager.runExecutionContext(context);
        }
    }

    private void closeTransaction(ChannelHandlerContext ctx, ThreadMessage msg) {
        // FIXME close of transaction have some errors
        Preconditions.checkState(transactionBinding.containsKey(msg.getSignature()));
        ThreadExecutionContext context = transactionBinding.remove(msg.getSignature());
//        context.exitGrobalTransactionContext();
        // this is a normal stop for the transaction, so expect it not running any more
        Preconditions.checkState(!context.isExecutionRunning());
    }

    private void initializeTransaction(ChannelHandlerContext ctx, ThreadMessage msg) {
        ThreadExecutionContext context;
        Preconditions.checkState(!transactionBinding.containsKey(msg.getSignature()));
        logger.trace("initializeTransaction. Create context on base of message {}", msg);
        // the pipe signature is used as key, because a transaction can have multiple service instance running in one server node
        context = contextManager.openExecutionContextForProtocolExecution(msg.getSignature(), msg.getUuid(), msg.getTargetUrl(), ctx.channel());
        context.initGrobalTransactionContext();
        transactionBinding.put(msg.getSignature(), context);
    }

}


class ThreadConnectionManager{

    private final ConcurrentMap<String, ThreadClientConnection> clientConnections;
    private final ModelFactory model;

    private final Map<String, ThreadMessagePipe> pipeRoutingMap = Maps.newConcurrentMap();

    @Inject
    ThreadConnectionManager(ModelFactory model) {
        this.model = model;
        clientConnections = Maps.newConcurrentMap();
    }

    ThreadClientConnection openConnection(URL address) {
        String key = address.getHost() + ":" + address.getPort();
        if (!clientConnections.containsKey(key)) {
            ThreadClientConnection serverConnection = model.openConnection(address);
            clientConnections.putIfAbsent(key, serverConnection);
        }
        return clientConnections.get(key);
    }

     List<ThreadMessagePipe> openPipes(UUID transactionID, List<URL> endpointAdressList) {
        Builder<ThreadMessagePipe> builder = ImmutableList.builder();
        for (URL address : endpointAdressList) {
            ThreadClientConnection connection = openConnection(address);
            String signature = newUniquePipeTransactionSignature(transactionID, address);
            ThreadMessagePipe threadMessagePipe = connection.bindPipe(signature, transactionID,address);
            builder.add(threadMessagePipe);
        }
        return builder.build();
    }

     ThreadMessagePipe createHeadPipe(String signature, Channel channel) {
         ThreadMessagePipe headPipe = model.createPipe(channel, signature,null,null);
         pipeRoutingMap.put(headPipe.getSignature(), headPipe);
         return headPipe;
     }

    AtomicInteger signatureCounter = new AtomicInteger();
    private String newUniquePipeTransactionSignature(UUID transactionID, URL endpointAddress){
        // TODO make a more effective unique signature pipe
        int counter = signatureCounter.getAndAdd(1);
        return counter + ":"+ transactionID.toString() + "-->"+ endpointAddress.toString();
    }

}

class ClientConnectionHandler extends ChannelInboundMessageHandlerAdapter<ThreadMessage> {

    private final Map<String, ThreadMessagePipe> pipeRoutingMap = Maps.newConcurrentMap();

    void bindPipe(String signature, ThreadMessagePipe pipe) {
        ThreadMessagePipe previous = pipeRoutingMap.put(signature, pipe);
        assert previous == null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, ThreadMessage msg) throws Exception {
        ThreadMessage.ThreadProtocolMessageType messageType = msg.getMessageType();
        switch (messageType) {
            case INITIALIZE_TRANSACTION:
            case CLOSE_TRANSACTION:
                throw new IllegalStateException();
            case CANONICAL_PROTOCOL_EXECUTION:
                routeMessage(msg);
                break;
            case EXCEPTION_MESSAGE:
                throw (Exception) msg.getPayload();
        }
    }

    private void routeMessage(ThreadMessage msg) {
        // TODO multiplex responce on calls
        ThreadMessagePipe clientPipe = pipeRoutingMap.get(msg.getSignature());
        Preconditions.checkNotNull(clientPipe);
        clientPipe.receiveMessage(msg);
    }
}


class ThreadClientConnection {

    private final Channel channel;

    @InjectLogger
    private Logger logger;

    private final ClientConnectionHandler clientConnectionHandler;

    private final ModelFactory model;

    @Inject
    ThreadClientConnection(@Assisted final URL address, ClientConnectionHandler handler, ModelFactory model) {
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

    ThreadMessagePipe bindPipe(String signature, UUID transactionID, URL address) {
        Preconditions.checkNotNull(signature);
        Preconditions.checkNotNull(transactionID);
        ThreadMessagePipe pipe = model.createPipe(channel,signature,transactionID,address);
        clientConnectionHandler.bindPipe(signature,pipe);
        return pipe;
    }

}

class ExecutionContextManager {
    private final ConcurrentMap<UUID, ThreadExecutionContext> localExecutionMap;
    private final ConcurrentMap<String, ThreadExecutionContext> externalExecutionMap;
    private final InternalLogicContextFactory internalLogicFactory;
    private final ModelFactory factory;
    private final ThreadConnectionManager connectionManager;

    @Inject
    ExecutionContextManager(InternalLogicContextFactory internalLogicFactory, ModelFactory factory, ThreadConnectionManager connectionManager) {
        super();
        this.localExecutionMap = Maps.newConcurrentMap();
        this.externalExecutionMap = Maps.newConcurrentMap();
        this.internalLogicFactory = internalLogicFactory;
        this.factory = factory;
        this.connectionManager = connectionManager;
    }

    UUID createExecutionContextForServiceInteraction(URL transactionURL) {
        return openExecutionContextForServiceInteraction(UUID.randomUUID(), transactionURL).getTransactionID();
    }

    ThreadExecutionContext openExecutionContextForServiceInteraction(UUID transactionID, URL transactionURL){
        Preconditions.checkNotNull(transactionID);
        Preconditions.checkNotNull(transactionURL);
        if (!localExecutionMap.containsKey(transactionID)) {
            ThreadExecutionContext context = createExecutionContext(null,transactionID, transactionURL, null);
            localExecutionMap.putIfAbsent(transactionID, context);
        }
        return localExecutionMap.get(transactionID);
    }

    ThreadExecutionContext openExecutionContextForProtocolExecution(String headPipeSignature, UUID transactionID, URL transactionURL, Channel channel) {
        Preconditions.checkNotNull(headPipeSignature);
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(transactionID);
        Preconditions.checkNotNull(transactionURL);
        return internalOpenExecutionContext(headPipeSignature,transactionID,transactionURL,channel);

    }
    private ThreadExecutionContext internalOpenExecutionContext(String headPipeSignature, UUID transactionID, URL transactionURL, Channel channel) {
        if (!externalExecutionMap.containsKey(headPipeSignature)) {
            ThreadExecutionContext context = createExecutionContext(headPipeSignature,transactionID, transactionURL, channel);
            externalExecutionMap.putIfAbsent(headPipeSignature, context);
        }
        return externalExecutionMap.get(headPipeSignature);
    }


    private ThreadExecutionContext createExecutionContext(String headPipeSignature,UUID transactionID, URL transactionURL, Channel channel) {
        ExecutionContextInternalLogic internalLogic = internalLogicFactory.open(transactionURL);
        ThreadMessagePipe headPipe = null;
        if(channel != null ) {
             headPipe = connectionManager.createHeadPipe(headPipeSignature,channel);
        }
        List<ThreadMessagePipe> endpoints = connectionManager.openPipes(transactionID, internalLogic.getEndpointAdressList());
        ThreadExecutionContext context = factory.threadExecutionContext(internalLogic, headPipe, endpoints,transactionID);
        return context;
    }

    public ThreadExecutionContext getServiceInteractionContext(UUID executionContextID) {
        return localExecutionMap.get(executionContextID);
    }
}

class ThreadExecutionManager {

    private final ExecutorService pool;
    private final ExecutionContextManager contextManager;
    @InjectLogger
    private Logger logger;

    @Inject
    ThreadExecutionManager(ExecutorService pool, ExecutionContextManager contextManager) {
        this.pool = pool;
        this.contextManager = contextManager;
    }

    <R, T> Future<R> executeServiceAction(ServiceAction<R, T> interaction) {
        UUID executionContextID = contextManager.createExecutionContextForServiceInteraction(interaction.getServiceURL());
        return pool.submit(new ServiceInteractionThreadWrapper<R, T>(interaction, contextManager.getServiceInteractionContext(executionContextID)));
    }

    void runExecutionContext(ThreadExecutionContext context) {
        pool.submit(new TransactionExecutionThreadWrapper(context));
    }

    private class ServiceInteractionThreadWrapper<R, T> implements Callable<R> {
        private final ServiceAction<R, T> interaction;
        private final ThreadExecutionContext executionContext;

        ServiceInteractionThreadWrapper(ServiceAction<R, T> interaction, ThreadExecutionContext executionContext) {
            super();
            this.interaction = interaction;
            this.executionContext = executionContext;
        }

        @Override
        public R call() throws Exception {
            logger.trace("execution thread running for service interaction");
            try {
                executionContext.initGrobalTransactionContext();
                if (executionContext.enterExecutionContext()) {
                    logger.trace("execution is open");
                    logger.trace("enter to service interaction");
                    return executionContext.executeInteraction(interaction);
                }
            } catch (Exception e) {
                logger.error("execution exceptions", e);
            } finally {
                executionContext.exitTransactionContext();
                executionContext.exitGrobalTransactionContext();
                logger.trace("execution closed");
            }
            // this should not happen
            logger.error("it was not possible to open the transaction for service interaction. Fatal exceptions");
            throw new RuntimeException("fatal exceptions on transaction open for service interaction");
        }
    }

    private class TransactionExecutionThreadWrapper implements Callable<Void> {
        private final ThreadExecutionContext executionContext;

        TransactionExecutionThreadWrapper(ThreadExecutionContext executionContext) {
            super();
            this.executionContext = executionContext;
        }

        @Override
        public Void call() throws Exception {
            logger.trace("execution thread running for canonical protocol execution");
                try {
                    if (executionContext.enterExecutionContext()) {
                        logger.trace("execution is open");
                        logger.trace("start canonical protocol execution");
                        executionContext.executeCanonicalProtocol();
                        return null;
                    }
                } catch (Exception e) {
                    logger.error("execution exceptions", e);   // FIXME exception policy.
                    // TODO expand exceptions to global transaction context
                } finally {
                    executionContext.exitTransactionContext();
                    logger.trace("execution closed");
                }
            logger.info("execution already open, ignoring duplicate execution start");
            // FIXME exception policy.
            return null;
        }
    }

}

class ThreadExecutionContext {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutionContextInternalLogic internalLogic;
    // Head pipe is null for execution context directly connected to service interactions on local server
    private final ThreadMessagePipe headCommandPipe;
    private final List<ThreadMessagePipe> endpoints;
    private final UUID transactionID;
    @InjectLogger
    private Logger logger;

    @Inject
    ThreadExecutionContext(@Assisted ExecutionContextInternalLogic internalLogic, @Nullable @Assisted ThreadMessagePipe headCommandPipe,
                           @Assisted List<ThreadMessagePipe> endpoints, @Assisted UUID transactionID) {
        super();
        this.internalLogic = internalLogic;
        this.headCommandPipe = headCommandPipe;
        this.endpoints = endpoints;
        this.transactionID = transactionID;
    }

    public UUID getTransactionID() {
        return transactionID;
    }

    ThreadMessagePipe getHeadCommandPipe() {
        return this.headCommandPipe;
    }


    <T> T getInstance(Key<T> key) {
        return internalLogic.getInstance(key);
    }

    <R, T> R executeInteraction(ServiceAction<R, T> interaction) {
        T instance = getInstance(interaction.getInterfaceKey());
        return interaction.executeInteraction(instance);
    }

    void executeCanonicalProtocol() {
        internalLogic.executeCanonicalProtocol();
    }

    boolean enterExecutionContext() {
        if (running.compareAndSet(false, true)) {
            internalLogic.enterExecution(headCommandPipe, endpoints);
            return true;
        }
        return false;
    }

    void exitTransactionContext() {
        if (running.compareAndSet(true, false)) {
            internalLogic.exitExecution();
        }
    }

    boolean isExecutionRunning() {
        return running.get();
    }

    public void initGrobalTransactionContext() {
        internalLogic.initTransaction();
        for (int i = 0; i < endpoints.size(); i++) {
            ThreadMessagePipe pipe = endpoints.get(i);
            pipe.initializeTransactionConnection();
        }
    }

    public void exitGrobalTransactionContext() {
        for (int i = 0; i < endpoints.size(); i++) {
            ThreadMessagePipe pipe = endpoints.get(i);
            pipe.closeTransactionConnection();
        }
        internalLogic.closeTransaction();

    }
}