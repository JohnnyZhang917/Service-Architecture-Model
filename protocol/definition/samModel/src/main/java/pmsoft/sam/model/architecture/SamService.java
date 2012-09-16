package pmsoft.sam.model.architecture;

import java.util.Set;


public interface SamService {

	public ServiceKey getServiceKey();
	
	public Set<Class<?>> getServiceInterfaces();
	
}
