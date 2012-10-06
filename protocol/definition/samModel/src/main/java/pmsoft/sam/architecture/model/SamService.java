package pmsoft.sam.architecture.model;

import java.util.Set;

import com.google.inject.Key;


public interface SamService {

	/**
	 * Key identifying service definition
	 * @return
	 */
	public ServiceKey getServiceKey();
	
	/**
	 * Set of interfaces provided by this service
	 * @return
	 */
	public Set<Key<?>> getServiceContractAPI();
	
}
