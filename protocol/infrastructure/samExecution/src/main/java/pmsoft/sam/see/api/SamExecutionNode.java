package pmsoft.sam.see.api;

import java.util.Set;

import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.model.ServiceInstanceMetadata;

public interface SamExecutionNode {

	/**
	 * Create a new instance of the given implementation with provided metadata.
	 * 
	 * @param key the service implementation key as registered on the ServiceRegistry
	 * @param metadata Metadata instance for the new Service Instance, null is interpreted as empty metadata
	 * @return the new ServiceInstance reference
	 */
	public SamServiceInstance createServiceInstance(SamServiceImplementationKey key, ServiceInstanceMetadata metadata);
	
	/**
	 * Returns all Service Instance created from the service implementation identified by key and matching metadata
	 * @param key Service Implementation used to create the service Instances
	 * @param metadata Required matching Metadata
	 * @return
	 */
	public Set<SamServiceInstance> searchInstance(SamServiceImplementationKey key, ServiceInstanceMetadata metadata);
	
}
