package pmsoft.sam.see.api.model;

import java.util.Set;

import pmsoft.sam.architecture.model.ServiceKey;


import com.google.inject.Injector;
import com.google.inject.Key;

public interface SamServiceInstance {

	public Injector getInjector();

	public SIID getKey();
	
	public ServiceMetadata getMetadata();

	public Set<Key<?>> getServiceContract();
	
	public ServiceKey getServiceKeyContract();
	
	public SamServiceImplementationKey getImplementationKey();
	
	// TODO maybe not necessary
	// public ServiceImplementation getImplementation();
}
