package pmsoft.sam.see.api.transaction;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIID;

/**
 * API to access a existing injection transaction.
 * 
 * @author pawel
 * 
 */
public interface SamInjectionConfiguration extends SamTransactionModelVisitable {
	
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
	 * Inform if there is some binding point used to inject services
	 * @return
	 */
	public boolean hasBindingPoints();
	
}
