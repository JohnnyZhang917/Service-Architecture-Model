package pmsoft.sam.see.api.transaction;

import java.util.Map;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;

/**
 * API to access a existing injection transaction.
 * 
 * @author pawel
 * 
 */
public interface SamInjectionTransactionConfiguration {
	
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
	public Map<ServiceKey, SamInjectionTransactionConfiguration> getNestedConfiguration();

}
