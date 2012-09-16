package pmsoft.sam.module.see;

import java.net.URL;
import java.util.List;

import pmsoft.sam.model.architecture.SamService;
import pmsoft.sam.model.instance.SIID;
import pmsoft.sam.model.instance.ServiceImplementationKey;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;

public interface ServiceExecutionEnviroment {

	public SIID executeServiceInstance(ServiceImplementationKey implementationKey);

	public List<ServiceInstance> getInstanceForService(SamService service);

	public ServiceInstance getServiceInstance(SIID instanceKey);

	public TransactionConfigurator createTransactionConfiguration(SIID rootInstance);

	public ExternalServiceReference getExternalServiceReference(URL targetURL);

	public SamTransaction getSamTransaction(URL url);

}
