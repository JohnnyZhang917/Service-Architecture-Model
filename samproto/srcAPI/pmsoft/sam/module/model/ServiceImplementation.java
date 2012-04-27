package pmsoft.sam.module.model;

import java.util.List;

import com.google.inject.Module;

public interface ServiceImplementation {

	public Class<? extends Module> getRootModule();

	public ServiceImplementationKey getKey();
	
	public ServiceKey getSpecificationKey();
	
	public List<ServiceKey> getInjectedServices();
}
