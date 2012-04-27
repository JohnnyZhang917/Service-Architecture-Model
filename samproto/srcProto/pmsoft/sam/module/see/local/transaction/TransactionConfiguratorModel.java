package pmsoft.sam.module.see.local.transaction;

import pmsoft.sam.module.model.SIID;
import pmsoft.sam.module.see.transaction.InjectionConfiguration;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class TransactionConfiguratorModel implements TransactionConfigurator {

	private ServiceInstanceConfiguration rootConfiguration;

	private final TransactionDomainFactory domainFactory;

	boolean open = true;

	@Inject
	public TransactionConfiguratorModel(@Assisted SIID rootServiceInstance, TransactionDomainFactory domainFactory) {
		super();
		this.domainFactory = domainFactory;
		rootConfiguration = this.domainFactory.createServiceConfiguration(rootServiceInstance);
	}

	public SamTransaction createTransactionContext() {
		Preconditions.checkState(open);
		open = false;
		return domainFactory.createTransactionInstance(rootConfiguration);
	}

	public InjectionConfiguration getInjectionConfiguration() {
		return rootConfiguration;
	}

}
