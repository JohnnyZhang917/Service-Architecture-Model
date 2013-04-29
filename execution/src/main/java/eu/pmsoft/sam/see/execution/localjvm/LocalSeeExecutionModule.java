package eu.pmsoft.sam.see.execution.localjvm;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.pmsoft.execution.ThreadExecutionModule;
import eu.pmsoft.sam.protocol.record.CanonicalProtocolModule;
import eu.pmsoft.sam.see.api.setup.SamExecutionNode;
import eu.pmsoft.sam.see.api.setup.SamExecutionNodeInternalApi;

public class LocalSeeExecutionModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(SamExecutionNodeInternalApi.class).to(SamExecutionNodeJVM.class);
//        bind(SamExecutionApi.class).to(SamExecutionApiJVM.class);
//
//        install(
//                new FactoryModuleBuilder()
//                        .implement(SamExecutionApi.class, SamExecutionApiJVM.class)
//                        .build(LocalSeeExecutionNodeModel.class)
//        );


        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!! TODO
        install(new ThreadExecutionModule());


        install(new CanonicalProtocolModule());
//        expose(SamServiceRegistry.class);
//        expose(SamExecutionNode.class);
    }

    @Provides
    public final SamExecutionNode getSamExecutionNode(SamExecutionNodeInternalApi executionNode) {
        return executionNode;
    }

}
