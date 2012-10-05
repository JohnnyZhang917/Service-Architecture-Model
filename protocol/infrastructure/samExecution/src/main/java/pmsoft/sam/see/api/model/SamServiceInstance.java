package pmsoft.sam.see.api.model;

import com.google.inject.Injector;

public interface SamServiceInstance {

	public Injector getInjector();

	public SIID getKey();
	
	public ServiceMetadata getMetadata();

	// TODO maybe not necessary
	// public ServiceImplementation getImplementation();
}
