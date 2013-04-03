package pmsoft.execution;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import pmsoft.injectionUtils.logger.LoggerInjectorModule;

public class ThreadExecutionModule extends AbstractModule {


    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(ThreadExecutionInfrastructure.class).asEagerSingleton();
        bind(ExecutionContextManager.class);
        bind(ThreadExecutionManager.class);
        bind(ProviderConnectionHandler.class);
        bind(ClientConnectionHandler.class);
        bind(ThreadConnectionManager.class).asEagerSingleton();
        install(new FactoryModuleBuilder().build(ModelFactory.class));
        install(new LoggerInjectorModule());

    }

}
