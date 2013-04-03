package eu.pmsoft.sam.protocol.record;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import eu.pmsoft.injectionUtils.logger.LoggerInjectorModule;
import eu.pmsoft.sam.protocol.CanonicalProtocolExecutionContext;
import eu.pmsoft.sam.protocol.CanonicalProtocolInfrastructure;
import eu.pmsoft.sam.protocol.TransactionController;

public class CanonicalProtocolModule extends PrivateModule {

    @Override
    protected void configure() {
        install(new LoggerInjectorModule());
        binder().requireExplicitBindings();
        bind(CanonicalProtocolInfrastructure.class).to(CanonicalProtocolModel.class).asEagerSingleton();
        install(new FactoryModuleBuilder()
                .implement(CanonicalProtocolExecutionContext.class, CanonicalProtocolExecutionContextObject.class)
                .implement(TransactionController.class, TransactionControllerImpl.class)
                .build(InjectionFactoryRecordModel.class));
        expose(CanonicalProtocolInfrastructure.class);
    }

}
