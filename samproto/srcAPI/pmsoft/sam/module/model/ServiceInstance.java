package pmsoft.sam.module.model;

import com.google.inject.Injector;

public interface ServiceInstance {

	public Injector getInjector();
	
	public SIID getKey();
}
