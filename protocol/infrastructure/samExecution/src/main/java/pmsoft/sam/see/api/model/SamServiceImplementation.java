package pmsoft.sam.see.api.model;

import java.util.Set;

import pmsoft.sam.architecture.model.ServiceKey;

import com.google.inject.Module;

public interface SamServiceImplementation {

	public Class<? extends Module> getModule();

	public SamServiceImplementationKey getKey();
	
	public ServiceKey getSpecificationKey();
	
	public Set<ServiceKey> getBindedServices();
}
