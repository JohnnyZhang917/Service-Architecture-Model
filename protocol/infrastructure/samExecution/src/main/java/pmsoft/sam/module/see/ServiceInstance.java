package pmsoft.sam.module.see;

import pmsoft.sam.model.instance.SIID;

import com.google.inject.Injector;

public interface ServiceInstance {

	public Injector getInjector();
	
	public SIID getKey();
}
