package eu.pmsoft.sam.see.configuration;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import eu.pmsoft.sam.see.api.model.*;
import eu.pmsoft.sam.see.api.setup.SamExecutionNodeInternalApi;
import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

public abstract class SEEServiceSetupAction {

    private SamExecutionNodeInternalApi executionNode;

    public final void setupService(SamExecutionNodeInternalApi executionNode) {
        Preconditions.checkNotNull(executionNode);
        try {
            this.executionNode = executionNode;
            setup();
        } finally {
            this.executionNode = null;
        }
    }

    public abstract void setup();

    protected final SIID createServiceInstance(Class<? extends Module> implementationModule) {
        return createServiceInstance(new SamServiceImplementationKey(implementationModule));
    }

    protected final SIID createServiceInstance(SamServiceImplementationKey implementationKey) {
        SamServiceInstance serviceInstance = executionNode.createServiceInstance(implementationKey, null);
        return serviceInstance.getKey();
    }

    protected final SamServiceInstanceTransaction setupServiceTransaction(SamInjectionConfiguration configuration) {
        Preconditions.checkNotNull(configuration);
        STID url = executionNode.setupInjectionTransaction(configuration);
        SamServiceInstanceTransaction transactionRegistered = executionNode.getTransaction(url);
        return transactionRegistered;
    }

}
