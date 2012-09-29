package pmsoft.sam.see.api.model;

import java.util.Map;

import pmsoft.sam.architecture.model.ServiceKey;

/**
 * API to access a existing injection transaction.
 * 
 * @author pawel
 * 
 */
public interface SamInjectionTransaction {

	/**
	 * Service contract provided by this transaction to the client. Only one
	 * contract is provided to client.
	 * 
	 * @return
	 */
	public ServiceKey getProvidedService();

	/**
	 * Service Instance providing the main contract exposed by this transaction
	 * 
	 * @return
	 */
	public SIID getExposedServiceInstance();

	/**
	 * Internal injection binding configuration. Immutable.
	 * 
	 * @return
	 */
	public Map<ServiceKey, SIID> getInternalInjectionConfiguration();

	/**
	 * External injection binding configuration. Immutable.
	 * 
	 * @return
	 */
	public Map<ServiceKey, SIURL> getExternalInjectionConfiguration();

	/**
	 * External injection binding configuration. Immutable.
	 * 
	 * @return
	 */
	public Map<ServiceKey, SamInjectionTransaction> getNestedConfiguration();

}
