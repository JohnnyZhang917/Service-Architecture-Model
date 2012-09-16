package pmsoft.sam.module.see.local;

import pmsoft.sam.model.instance.SIID;
import pmsoft.sam.module.see.ServiceInstance;

import com.google.inject.Injector;

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
