package eu.pmsoft.sam.protocol.record;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import eu.pmsoft.sam.protocol.CanonicalProtocolRecordingModel;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.protocol.TransactionController;

public class CanonicalProtocolModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        install(new FactoryModuleBuilder()
                .implement(CanonicalProtocolThreadExecutionContext.class, CanonicalProtocolThreadExecutionContextObject.class)
                .implement(TransactionController.class, TransactionControllerImpl.class)
                .build(InjectionFactoryRecordModel.class));
        bind(CanonicalProtocolRecordingModel.class).to(CanonicalProtocolModel.class).asEagerSingleton();
//        expose(CanonicalProtocolInfrastructure.class);
    }

}
