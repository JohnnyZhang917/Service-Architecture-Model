package pmsoft.sam.model.instance;

import java.util.List;

import pmsoft.sam.architecture.model.ServiceKey;

import com.google.inject.Module;

public interface ServiceImplementation {

	public Class<? extends Module> getRootModule();

	public ServiceImplementationKey getKey();
	
	public ServiceKey getSpecificationKey();
	
	public List<ServiceKey> getInjectedServices();
}
