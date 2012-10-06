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
	 * @return
	 */
	public Class<? extends Module> getModule();

	/**
	 * Identification key of implementation
	 * @return
	 */
	public SamServiceImplementationKey getKey();
	
	/**
	 * Service contract provided by this service
	 * @return
	 */
	public ServiceKey getSpecificationKey();
	
	/**
	 * List of binded services in natural order
	 * @return
	 */
	public List<ServiceKey> getBindedServices();
}
