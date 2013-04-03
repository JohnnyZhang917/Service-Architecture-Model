package pmsoft.sam.see.execution.localjvm;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import pmsoft.sam.protocol.record.CanonicalProtocolModule;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import pmsoft.sam.see.api.SamServiceRegistry;

public class LocalSeeExecutionModule extends PrivateModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(SamExecutionNodeInternalApi.class).to(SamExecutionNodeJVM.class).asEagerSingleton();
        install(new CanonicalProtocolModule());
        expose(SamServiceRegistry.class);
        expose(SamExecutionNode.class);
    }

    @Provides
    public final SamServiceRegistry getSamServiceRegistry(SamExecutionNodeInternalApi executionNode) {
        return executionNode;
    }

    @Provides
    public final SamExecutionNode getSamExecutionNode(SamExecutionNodeInternalApi executionNode) {
        return executionNode;
    }

}
