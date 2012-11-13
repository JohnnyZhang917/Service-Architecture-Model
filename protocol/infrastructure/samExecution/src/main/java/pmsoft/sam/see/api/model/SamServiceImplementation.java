package pmsoft.sam.see.api.model;

import java.util.List;

import pmsoft.sam.architecture.model.ServiceKey;

import com.google.inject.Module;

/**
 * Definition of a Service Implementation
 * @author pawel
 *
 */
public interface SamServiceImplementation {

	/**
	 * Implementation injection configuration provided as a Guice module
	 * @return class defining implementation module
	 */
	public Class<? extends Module> getModule();

	/**
	 * Identification key of implementation
	 * @return key of the implementation
	 */
	public SamServiceImplementationKey getKey();
	
	/**
	 * Service contract provided by this service
	 * @return key of the service contract
	 */
	public ServiceKey getSpecificationKey();
	
	/**
	 * List of binded services in natural order
	 * @return list of used services keys
	 */
	public List<ServiceKey> getBindedServices();
}
