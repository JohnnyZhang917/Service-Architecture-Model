package eu.pmsoft.execution;


public final class ThreadExecutionServer {
//
//    private final ThreadExecutionManager executionService;
//    private final ServerBootstrap serverBootstrap;
//    private final Provider<ProviderConnectionHandler> providerServerConnectionHandler;
//    private final InetSocketAddress serverAddress;
//
//    @InjectLogger
//    private Logger logger;
//
//    @Inject
//    ThreadExecutionServer(ThreadExecutionManager executionService, Provider<ProviderConnectionHandler> providerServerConnectionHandler, SEEConfiguration configuration) {
//        super();
//        this.executionService = executionService;
//        this.serverAddress = null;
////        this.serverAddress = configuration.address;
//        this.serverBootstrap = new ServerBootstrap();
//        this.providerServerConnectionHandler = providerServerConnectionHandler;
//    }
//
//    public void startServer() {
////        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
////        if( serverAddress != null ){
////            logger.debug("starting local server on address {}", serverAddress);
////            // TODO async bind to socket address
////            serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup()).channel(NioServerSocketChannel.class).localAddress(serverAddress)
////                    .childHandler(new ChannelInitializer<SocketChannel>() {
////                        @Override
////                        public void initChannel(SocketChannel ch) throws Exception {
////                            ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE)).addLast(new WriteTimeoutHandler(3000)).addLast(new ReadTimeoutHandler(3000))
////                                    .addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), providerServerConnectionHandler.get());
////                        }
////                    });
////            Channel channel = serverBootstrap.bind().syncUninterruptibly().channel();
////            assert channel != null;
////        }
//    }
//
//    public void shutdownServer() {
////        if( serverAddress != null ){
////            logger.debug("shutdown local server on address {}", serverAddress);
////            serverBootstrap.shutdown();
////        }
//    }
//
////    public <R, T> Future<R> executeServiceAction(ServiceAction<R, T> serviceAction) {
////        //TODO po co takie proste przekazanie
////        return executionService.executeServiceAction(serviceAction);
////    }
//
////    public String getServerEndpointBase() {
////        // TODO delete me
////        if( serverAddress == null) {
////            // TODO client mode execution marked as global state
////            return "localhost:0000";
////        }
////        return serverAddress.getHostName() + ":" + serverAddress.getPort();
////    }
}


