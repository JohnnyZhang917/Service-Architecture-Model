package pmsoft.sam.see;

import com.google.common.base.Preconditions;
import com.google.inject.Module;

import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInstanceTransaction;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

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

	protected final SamInstanceTransaction setupServiceTransaction(SamInjectionConfiguration configuration) {
		SIURL url = executionNode.setupInjectionTransaction(configuration);
		SamInstanceTransaction transactionRegistered = executionNode.getTransaction(url);
		return transactionRegistered;
	}

}
