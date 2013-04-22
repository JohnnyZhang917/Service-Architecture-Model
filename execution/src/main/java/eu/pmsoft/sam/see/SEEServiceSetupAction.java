package eu.pmsoft.sam.see;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import eu.pmsoft.execution.ServiceEndpointAddressProvider;
import eu.pmsoft.execution.ThreadExecutionServer;
import eu.pmsoft.sam.see.api.SamExecutionNode;
import eu.pmsoft.sam.see.api.model.*;
import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

public abstract class SEEServiceSetupAction {

    private SamExecutionNode executionNode;

    public final void setupService(SamExecutionNode executionNode) {
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

    protected final SamInstanceTransaction setupServiceTransaction(SamInjectionConfiguration configuration, ExecutionStrategy executionStrategy) {
        Preconditions.checkNotNull(configuration);
        Preconditions.checkNotNull(executionStrategy);
        SIURL url = executionNode.setupInjectionTransaction(configuration, executionStrategy);
        SamInstanceTransaction transactionRegistered = executionNode.getTransaction(url);
        return transactionRegistered;
    }

}
