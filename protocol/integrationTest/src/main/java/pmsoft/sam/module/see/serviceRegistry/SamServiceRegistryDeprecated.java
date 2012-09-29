package pmsoft.sam.module.see.serviceRegistry;

import java.util.List;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.implementation.SamServiceImplementationContract;
import pmsoft.sam.see.api.model.ServiceImplementation;
import pmsoft.sam.see.api.model.ServiceImplementationKey;

public interface SamServiceRegistryDeprecated {

	public void registerServiceImplementation(SamServiceImplementationContract definition);

	public void unRegisterServiceImplementation(SamServiceImplementationContract definition);

	public List<ServiceImplementationKey> getImplementationsForSpecification(ServiceKey key);

	public ServiceImplementation getImplementation(ServiceImplementationKey implementationKey);

}
