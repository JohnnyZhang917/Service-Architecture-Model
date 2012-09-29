package pmsoft.sam.module.see.local.transaction;

import java.net.URL;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;
import pmsoft.sam.see.api.model.SIID;

public interface TransactionDomainFactory {
	
	public TransactionInstance createTransactionInstance(InjectionConfigurationInternal injectionConfiguration);

	public TransactionConfigurator createTransactionConfigurator(SIID rootInstance);

	public ServiceInstanceConfiguration createServiceConfiguration(SIID instanceIID);

	public ServiceExternalInstanceConfiguration createExternalServiceConfiguration(ServiceKey serviceSpecification, URL externalService);
}
