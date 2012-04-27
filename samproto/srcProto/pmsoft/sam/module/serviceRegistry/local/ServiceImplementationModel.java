package pmsoft.sam.module.serviceRegistry.local;

import java.util.List;

import com.google.inject.Module;

import pmsoft.sam.module.model.ServiceImplementation;
import pmsoft.sam.module.model.ServiceImplementationKey;
import pmsoft.sam.module.model.ServiceKey;

public class ServiceImplementationModel implements ServiceImplementation {

	private final ServiceKey specificationkey;
	private final ServiceImplementationKey implementationKey;
	private final Class<? extends Module> rootModule;
	private final List<ServiceKey> userServices;
	
	public ServiceImplementationModel(ServiceKey specificationkey, ServiceImplementationKey implementationKey,
			Class<? extends Module> rootModule, List<ServiceKey> userServices) {
		super();
		this.specificationkey = specificationkey;
		this.implementationKey = implementationKey;
		this.rootModule = rootModule;
		this.userServices = userServices;
	}

	public Class<? extends Module> getRootModule() {
		return rootModule;
	}

	public ServiceImplementationKey getKey() {
		return implementationKey;
	}

	public ServiceKey getSpecificationKey() {
		return specificationkey;
	}

	public List<ServiceKey> getInjectedServices() {
		return userServices;
	}

}
