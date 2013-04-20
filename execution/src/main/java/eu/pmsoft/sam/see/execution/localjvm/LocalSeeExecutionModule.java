package eu.pmsoft.sam.see.execution.localjvm;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.pmsoft.sam.protocol.record.CanonicalProtocolModule;
import eu.pmsoft.sam.see.api.SamExecutionNode;
import eu.pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import eu.pmsoft.sam.see.api.SamServiceRegistry;

public class LocalSeeExecutionModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(SamExecutionNodeInternalApi.class).to(SamExecutionNodeJVM.class).asEagerSingleton();
        bind(SamServiceRegistry.class).to(SamServiceRegistryLocal.class).asEagerSingleton();
        install(new CanonicalProtocolModule());
//        expose(SamServiceRegistry.class);
//        expose(SamExecutionNode.class);
    }

    @Provides
    public final SamExecutionNode getSamExecutionNode(SamExecutionNodeInternalApi executionNode) {
        return executionNode;
    }

}
