package pmsoft.sam.module.model;

import java.util.Set;


public interface SamService {

	public ServiceKey getServiceKey();
	
	public Set<Class<?>> getServiceInterfaces();
	
}
