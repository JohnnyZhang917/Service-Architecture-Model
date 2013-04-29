package eu.pmsoft.execution;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import eu.pmsoft.injectionUtils.logger.LoggerInjectorModule;

public class ThreadExecutionModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
//        bind(ThreadExecutionServer.class).asEagerSingleton();
        bind(ExecutionContextManager.class);
//        bind(ThreadExecutionManager.class);
//        bind(ProviderConnectionHandler.class);
        bind(ClientConnectionHandler.class);
        bind(ThreadConnectionManager.class).asEagerSingleton();
        install(new FactoryModuleBuilder().build(ThreadExecutionModelFactory.class));
    }

}
