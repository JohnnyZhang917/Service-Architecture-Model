package pmsoft.sam.module.see.serviceRegistry;

import java.util.List;

import pmsoft.sam.definition.instance.SamServiceImplementationContract;
import pmsoft.sam.model.architecture.ServiceKey;
import pmsoft.sam.model.instance.ServiceImplementation;
import pmsoft.sam.model.instance.ServiceImplementationKey;

public interface SamServiceRegistry {

	public void registerServiceImplementation(SamServiceImplementationContract definition);

	public void unRegisterServiceImplementation(SamServiceImplementationContract definition);

	public List<ServiceImplementationKey> getImplementationsForSpecification(ServiceKey key);

	public ServiceImplementation getImplementation(ServiceImplementationKey implementationKey);

}
