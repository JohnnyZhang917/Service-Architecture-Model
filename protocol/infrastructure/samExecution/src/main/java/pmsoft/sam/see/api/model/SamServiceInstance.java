package pmsoft.sam.see.api.model;

import java.util.Set;


import com.google.inject.Injector;
import com.google.inject.Key;

public interface SamServiceInstance {

	public Injector getInjector();

	public SIID getKey();
	
	public ServiceMetadata getMetadata();

	public Set<Key<?>> getServiceContract();
	
	// TODO maybe not necessary
	// public ServiceImplementation getImplementation();
}
