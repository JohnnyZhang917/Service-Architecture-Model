package eu.pmsoft.execution;


//import com.google.common.collect.ImmutableList;
//import com.google.inject.*;
//import com.google.inject.name.Named;
//import com.google.inject.name.Names;
//import eu.pmsoft.injectionUtils.logger.LoggerInjectorModule;
//import eu.pmsoft.sam.see.api.model.STID;
//import eu.pmsoft.sam.see.configuration.SEEConfiguration;
//import eu.pmsoft.sam.see.transport.SamTransportLayer;
//import eu.pmsoft.sam.see.transport.ServiceEndpointBinding;
//import eu.pmsoft.sam.see.transport.TransactionCommunicationContext;
//import eu.pmsoft.sam.see.transport.TransportChannel;
//import io.netty.util.internal.logging.InternalLoggerFactory;
//import io.netty.util.internal.logging.Slf4JLoggerFactory;
//import org.testng.annotations.Test;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
//import static org.testng.AssertJUnit.assertEquals;
//import static org.testng.AssertJUnit.assertNotNull;

@Deprecated
public class ApiTest {
//
//    @Test
//    public void localInjectorCreation() {
//        Injector injector = Guice.createInjector(new FakeInternalLogicModule());
//        assertNotNull(injector);
//        Binding<ThreadExecutionServer> serverBind = injector.getExistingBinding(Key.get(ThreadExecutionServer.class));
//        assertNotNull(serverBind);
//        ThreadExecutionServer server = injector.getInstance(ThreadExecutionServer.class);
//        server.startServer();
//        server.shutdownServer();
//    }
//
//    public static class FakeInternalLogicModule extends AbstractModule {
//
//        @Override
//        protected void configure() {
//            bind(ThreadExecutionLogicProvider.class).toInstance(new ThreadExecutionLogicProvider() {
//                @Override
//                public ThreadExecutionContextInternalLogic open(STID serviceEndpointURL) {
//                    return null;
//                }
//            });
//            bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(5));
//            bind(SEEConfiguration.class).toInstance(SEEConfiguration.empty());
//            install(new ThreadExecutionModule());
//            install(new LoggerInjectorModule());
//        }
//
//    }
//
//    static URL serviceOne;
//    static URL serviceTwo;
//
//    static {
//        try {
//            InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
//            serviceOne = new URL("http://localhost:5001/one");
//            serviceTwo = new URL("http://localhost:5002/two");
//        } catch (MalformedURLException e) {
//            // ignore, will fail tests
//        }
//    }
//
//    static List<URL> oneSendToTwoConfiguration(URL service) {
//        if (service == serviceOne) {
//            return ImmutableList.of(serviceTwo);
//        }
//        return ImmutableList.of();
//    }
//
//    @Test
//    public void executeSimpleCall() throws ExecutionException, InterruptedException {
//        Injector injectorOne = Guice.createInjector(new SimpleCallLogicModule(SEEConfiguration.empty()));
//        ThreadExecutionServer one = injectorOne.getInstance(ThreadExecutionServer.class);
//
//        one.startServer();
//        ThreadExecutionManager threadExecutionManager = injectorOne.getInstance(ThreadExecutionManager.class);
////        Future<String> testFuture = threadExecutionManager.executeServiceAction(new ServiceAction<String, SimpleCallService>(serviceOne, Key.get(SimpleCallService.class)) {
//        Future<String> testFuture = threadExecutionManager.executeServiceAction(new ServiceAction<String, SimpleCallService>(null, Key.get(SimpleCallService.class)) {
//            @Override
//            public String executeInteraction(SimpleCallService service) {
//                return service.makeSimpleCall();
//            }
//        });
//        assertEquals(serviceTwo.toString(), testFuture.get());
//        one.shutdownServer();
//
//    }
//
//    static class SimpleCallLogicModule extends AbstractModule {
//
//        private final SEEConfiguration configuration;
//
//        SimpleCallLogicModule(SEEConfiguration configuration) {
//            this.configuration = configuration;
//        }
//
//        @Override
//        protected void configure() {
//            bind(ThreadExecutionLogicProvider.class).to(SimpleThreadExecutionLogicProvider.class);
//            bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(5));
//            install(new ThreadExecutionModule());
//            bind(SEEConfiguration.class).toInstance(configuration);
//            install(new LoggerInjectorModule());
////            install(new FakeSamTransportLayerModule());
//        }
//    }
//
//    static class SimpleThreadExecutionLogicProvider implements ThreadExecutionLogicProvider {
//        private final SamTransportLayer transportLayer;
//
//        @Inject
//        SimpleThreadExecutionLogicProvider(SamTransportLayer transportLayer) {
//            this.transportLayer = transportLayer;
//        }
//
//        @Override
//        public ThreadExecutionContextInternalLogic open(STID serviceEndpointURL) {
////            return new SimpleExecutionLogic(serviceEndpointURL, transportLayer);
//            return null;
//        }
//    }
//
//    static class SimpleExecutionLogic implements ThreadExecutionContextInternalLogic, CallExternal {
//
//        private final URL serviceEndpointURL;
//        private final Injector injector;
//        private final List<URL> externalServices;
//        private final SamTransportLayer transportLayer;
//        private TransportChannel headChannel;
//        private TransportChannel endpointChannel;
//        private TransactionCommunicationContext transactionCommunicationContext;
//        private UUID transactionId;
//
//        @Inject
//        public SimpleExecutionLogic(final URL serviceEndpointURL, SamTransportLayer transportLayer) {
//            this.serviceEndpointURL = serviceEndpointURL;
//            this.transportLayer = transportLayer;
//            this.externalServices = oneSendToTwoConfiguration(serviceEndpointURL);
//            this.injector = Guice.createInjector(new AbstractModule() {
//                @Override
//                protected void configure() {
//                    bind(URL.class).annotatedWith(Names.named("serviceURL")).toInstance(serviceEndpointURL);
//                    bind(SimpleCallService.class);
//                    bind(CallExternal.class).toInstance(SimpleExecutionLogic.this);
//                }
//            });
//        }
//
//        @Override
//        public <T> T getInstance(Key<T> key) {
//            return injector.getInstance(key);
//        }
//
//        @Override
//        public String call() {
//            ThreadMessage message = new ThreadMessage();
//            message.setSourceUrl(serviceEndpointURL);
//            message.setTargetUrl(externalServices.get(0));
//            message.setTransactionId(this.transactionId);
//            endpointChannel.sendMessage(message);
//            ThreadMessage response = endpointChannel.waitResponse();
//            return new String(response.getPayload());
//        }
//
//
//        @Override
//        public void enterExecution() {
//        }
//
//        @Override
//        public void exitExecution() {
//        }
//
//        @Override
//        public void executeCanonicalProtocol() {
//            ThreadMessage msg = this.headChannel.pollMessage();
//            if (msg == null) {
//                throw new RuntimeException("message pool can not be empty for protocol execution");
//            }
//            SimpleCallService localService = injector.getInstance(SimpleCallService.class);
//            msg.setPayload(localService.getAddress().toString().getBytes());
//            this.headChannel.sendMessage(msg);
//        }
//
//        @Override
//        public void initTransaction(UUID transactionUniqueID) {
////            this.transactionId = transactionUniqueID;
//////            ServiceEndpointBinding serviceEndpointBinding = transportLayer.bindServiceInstance(serviceEndpointURL);
////            this.transactionCommunicationContext = transportLayer.openTransactionCommunicationContext(transactionUniqueID);
//////            this.headChannel = transactionCommunicationContext.openChannel(serviceEndpointURL);
////            if(! externalServices.isEmpty()) {
////                this.endpointChannel = transactionCommunicationContext.openChannel(externalServices.get(0));
////            }
//        }
//
//        @Override
//        public void closeTransaction() {
//            this.headChannel = null;
//            this.endpointChannel = null;
//            transactionCommunicationContext.closeContext();
//        }
//    }
//
//    static interface CallExternal {
//        String call();
//    }
//
//    static class SimpleCallService {
//
//        private final URL serviceURL;
//        private final CallExternal external;
//
//        @Inject
//        private SimpleCallService(@Named("serviceURL") URL serviceURL, CallExternal external) {
//            this.serviceURL = serviceURL;
//            this.external = external;
//        }
//
//        URL getAddress() {
//            return serviceURL;
//        }
//
//        String makeSimpleCall() {
//            return external.call();
//        }
//    }
}
