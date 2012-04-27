package pmsoft.sam.module.serviceRegistry;

import java.util.List;

import pmsoft.sam.module.definition.implementation.SamServiceImplementationDefinition;
import pmsoft.sam.module.model.ServiceImplementation;
import pmsoft.sam.module.model.ServiceImplementationKey;
import pmsoft.sam.module.model.ServiceKey;

public interface SamServiceRegistry {

	public void registerServiceImplementation(SamServiceImplementationDefinition definition);

	public void unRegisterServiceImplementation(SamServiceImplementationDefinition definition);

	public List<ServiceImplementationKey> getImplementationsForSpecification(ServiceKey key);

	public ServiceImplementation getImplementation(ServiceImplementationKey implementationKey);

}
