package pmsoft.sam.module.see.local;

import com.google.inject.Injector;

import pmsoft.sam.module.model.SIID;
import pmsoft.sam.module.model.ServiceInstance;

public class ServiceInstanceModel implements ServiceInstance {

	private final Injector injector;
	private final SIID key;
	
	public ServiceInstanceModel(Injector injector, SIID key) {
		super();
		this.injector = injector;
		this.key = key;
	}

	public Injector getInjector() {
		return injector;
	}

	public SIID getKey() {
		return key;
	}

}
