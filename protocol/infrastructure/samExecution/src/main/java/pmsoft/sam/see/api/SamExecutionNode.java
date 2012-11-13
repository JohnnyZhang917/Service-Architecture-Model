package pmsoft.sam.see.api;

import java.util.Set;
import java.util.UUID;

import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionContext;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInstanceTransaction;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.model.ServiceMetadata;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

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
	 * @return ServiceInstances that match the given key and metadata
	 * 
	 */
	public Set<SamServiceInstance> searchInstance(SamServiceImplementationKey key, ServiceMetadata metadata);

	/**
	 * Create a Injection Transaction
	 * 
	 * @param configuration
	 *            Binding configuration between services
	 * @param url
	 *            expected URL for this transaction
	 * 
	 * @return Confirmed URL for the new created transaction, it may differ from
	 *         url
	 */
	public SIURL setupInjectionTransaction(SamInjectionConfiguration configuration, SIURL url);

	public SIURL setupInjectionTransaction(SamInjectionConfiguration configuration);

	/**
	 * Publish to service Transaction on the ServiceDiscovery infrastructure
	 * binded to the ExecutionNode
	 * 
	 * @param url URL of the transaction
	 */
	public SamInstanceTransaction getTransaction(SIURL url);

	public CanonicalProtocolExecutionContext createTransactionExecutionContext(SIURL url);

	public CanonicalProtocolExecutionContext openTransactionExecutionContext(SIURL targetUrl, UUID transactionUniqueId, boolean forwardCall);

}
