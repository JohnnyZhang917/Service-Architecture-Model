package pmsoft.sam.module.see.local.transaction;

import java.net.URL;

import pmsoft.sam.module.model.SIID;
import pmsoft.sam.module.model.ServiceKey;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;

public interface TransactionDomainFactory {
	
	public TransactionInstance createTransactionInstance(InjectionConfigurationInternal injectionConfiguration);

	public TransactionConfigurator createTransactionConfigurator(SIID rootInstance);

	public ServiceInstanceConfiguration createServiceConfiguration(SIID instanceIID);

	public ServiceExternalInstanceConfiguration createExternalServiceConfiguration(ServiceKey serviceSpecification, URL externalService);
}
