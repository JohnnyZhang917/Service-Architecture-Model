package eu.pmsoft.execution;


import com.google.common.collect.ImmutableList;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import eu.pmsoft.injectionUtils.logger.LoggerInjectorModule;
import eu.pmsoft.sam.see.configuration.SEEConfiguration;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class ApiTest {

    @Test
    public void localInjectorCreation() {
        Injector injector = Guice.createInjector(new FakeInternalLogicModule());
        assertNotNull(injector);
        Binding<ThreadExecutionServer> serverBind = injector.getExistingBinding(Key.get(ThreadExecutionServer.class));
        assertNotNull(serverBind);
        ThreadExecutionServer server = injector.getInstance(ThreadExecutionServer.class);
        server.startServer();
        server.shutdownServer();
    }

    public static class FakeInternalLogicModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ThreadExecutionLogicProvider.class).toInstance(new ThreadExecutionLogicProvider() {
                @Override
                public ThreadExecutionContextInternalLogic open(URL targetURL) {
                    return null;
                }
            });
            bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(5));
            bind(SEEConfiguration.class).toInstance(SEEConfiguration.empty());
            install(new ThreadExecutionModule());
            install(new LoggerInjectorModule());
        }

    }

    static URL serviceOne;
    static URL serviceTwo;

    static {
        try {
            InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
            serviceOne = new URL("http://localhost:5001/one");
            serviceTwo = new URL("http://localhost:5002/two");
        } catch (MalformedURLException e) {
            // ignore, will fail tests
        }
    }

    static List<URL> oneSendToTwoConfiguration(URL service) {
        if (service == serviceOne) {
            return ImmutableList.of(serviceTwo);
        }
        return ImmutableList.of();
    }

    @Test
    public void executeSimpleCall() throws ExecutionException, InterruptedException {

        ThreadExecutionServer one = Guice.createInjector(new SimpleCallLogicModule(SEEConfiguration.empty())).getInstance(ThreadExecutionServer.class);
        ThreadExecutionServer two = Guice.createInjector(new SimpleCallLogicModule(SEEConfiguration.emptyOnPort(5002))).getInstance(ThreadExecutionServer.class);

        one.startServer();
        two.startServer();
        Future<String> testFuture = one.executeServiceAction(new ServiceAction<String, SimpleCallService>(serviceOne, Key.get(SimpleCallService.class)) {
            @Override
            public String executeInteraction(SimpleCallService service) {
                return service.makeSimpleCall();
            }
        });
        assertEquals(serviceTwo.toString(), testFuture.get());
        one.shutdownServer();
        two.shutdownServer();

    }

    static class SimpleCallLogicModule extends AbstractModule {

        private final SEEConfiguration configuration;

        SimpleCallLogicModule(SEEConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        protected void configure() {
            bind(ThreadExecutionLogicProvider.class).toInstance(new ThreadExecutionLogicProvider() {
                @Override
                public ThreadExecutionContextInternalLogic open(URL targetURL) {
                    return new SimpleExecutionlogic(targetURL);
                }
            });
            bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(5));
            install(new ThreadExecutionModule());
            bind(SEEConfiguration.class).toInstance(configuration);
            install(new LoggerInjectorModule());
        }
    }

    static class SimpleExecutionlogic implements ThreadExecutionContextInternalLogic, CallExternal {

        private final URL target;
        private final Injector injector;
        private final List<URL> externalServices;
        private ThreadMessagePipe headCommandPipe;
        private ThreadMessagePipe endpoint;

        public SimpleExecutionlogic(final URL targetURL) {
            this.target = targetURL;
            this.externalServices = oneSendToTwoConfiguration(target);
            this.injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(URL.class).annotatedWith(Names.named("serviceURL")).toInstance(targetURL);
                    bind(SimpleCallService.class);
                    bind(CallExternal.class).toInstance(SimpleExecutionlogic.this);
                }
            });
        }

        @Override
        public <T> T getInstance(Key<T> key) {
            return injector.getInstance(key);
        }

        @Override
        public String call() {
            ThreadMessage message = new ThreadMessage();
            endpoint.sendMessage(message);
            ThreadMessage response = endpoint.waitResponse();
            return new String(response.getPayload());
        }


        @Override
        public void enterExecution(ThreadMessagePipe headCommandPipe, List<ThreadMessagePipe> endpoints) {
            this.headCommandPipe = headCommandPipe;
            if (endpoints.size() > 0) {
                this.endpoint = endpoints.get(0);
            }
        }

        @Override
        public void exitExecution() {
            this.headCommandPipe = null;
            this.endpoint = null;
        }

        @Override
        public List<URL> getEndpointAddressList() {
            return externalServices;
        }

        @Override
        public void executeCanonicalProtocol() {
            ThreadMessage msg = this.headCommandPipe.pollMessage();
            if (msg == null) {
                throw new RuntimeException("message pool can not be empty for protocol execution");
            }
            SimpleCallService localService = injector.getInstance(SimpleCallService.class);
            msg.setPayload(localService.getAddress().toString().getBytes());
            this.headCommandPipe.sendMessage(msg);
        }

        @Override
        public void initTransaction() {
        }

        @Override
        public void closeTransaction() {
        }
    }

    static interface CallExternal {
        String call();
    }

    static class SimpleCallService {

        private final URL serviceURL;
        private final CallExternal external;

        @Inject
        private SimpleCallService(@Named("serviceURL") URL serviceURL, CallExternal external) {
            this.serviceURL = serviceURL;
            this.external = external;
        }

        URL getAddress() {
            return serviceURL;
        }

        String makeSimpleCall() {
            return external.call();
        }
    }
}
