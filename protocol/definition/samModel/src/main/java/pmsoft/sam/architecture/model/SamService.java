package pmsoft.sam.architecture.model;

import java.util.Set;

import com.google.inject.Key;


public interface SamService {

	public ServiceKey getServiceKey();
	
	public Set<Key<?>> getServiceContractAPI();
	
}
