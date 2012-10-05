package pmsoft.sam.see.api;

import java.util.Set;

import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.model.ServiceMetadata;
import pmsoft.sam.see.api.transaction.SamInjectionTransaction;
import pmsoft.sam.see.api.transaction.SamInjectionTransactionConfiguration;

public interface SamExecutionNode extends SamServiceRegistry {

	/**
	 * Create a new instance of the given implementation with provided metadata.
	 * 
	 * @param key
	 *            the service implementation key as registered on the
	 *            ServiceRegistry
	 * @param metadata
	 *            Metadata instance for the new Service Instance, null is
	 *            interpreted as empty metadata
	 * @return the new ServiceInstance reference
	 */
	public SamServiceInstance createServiceInstance(SamServiceImplementationKey key, ServiceMetadata metadata);

	/**
	 * Returns all Service Instance created from the service implementation
	 * identified by key and matching metadata
	 * 
	 * @param key
	 *            Service Implementation used to create the service Instances
	 * @param metadata
	 *            Required matching Metadata
	 * @return
	 */
	public Set<SamServiceInstance> searchInstance(SamServiceImplementationKey key, ServiceMetadata metadata);

	/**
	 * Create a Injection Transaction
	 * 
	 * @param configuration
	 *            Binding configuration between services
	 * @param metadata
	 *            Metadata of the injection transaction
	 * @return
	 */
	public void setupInjectionTransaction(SamInjectionTransactionConfiguration configuration, ServiceMetadata metadata, SIURL url);

	/**
	 * Publish to service Transaction on the ServiceDiscovery infrastructure
	 * binded to the ExecutionNode
	 * 
	 * @param transaction
	 * @param url
	 */
	public SamInjectionTransaction getTransaction(SIURL url);

}
