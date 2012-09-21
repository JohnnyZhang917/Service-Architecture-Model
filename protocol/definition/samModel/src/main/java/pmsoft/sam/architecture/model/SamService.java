package pmsoft.sam.architecture.model;

import java.util.Set;


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
	public Set<Class<?>> getServiceInterfaces();
	
}
