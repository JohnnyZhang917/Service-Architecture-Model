package pmsoft.sam.module.see;

import java.net.URL;
import java.util.List;

import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.ServiceImplementationKey;
import pmsoft.sam.see.api.model.ServiceInstance;

public interface ServiceExecutionEnviroment {

	public SIID executeServiceInstance(ServiceImplementationKey implementationKey);

	public List<ServiceInstance> getInstanceForService(SamService service);

	public ServiceInstance getServiceInstance(SIID instanceKey);

	public TransactionConfigurator createTransactionConfiguration(SIID rootInstance);

	public ExternalServiceReference getExternalServiceReference(URL targetURL);

	public SamTransaction getSamTransaction(URL url);

}
