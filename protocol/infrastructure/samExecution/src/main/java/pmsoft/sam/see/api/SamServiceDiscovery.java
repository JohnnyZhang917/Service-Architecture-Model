package pmsoft.sam.see.api;

import java.util.Map;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIURL;


public interface SamServiceDiscovery {

	public void serviceTransactionCreated(SIURL url, ServiceKey contract);
	
	public Map<SIURL, ServiceKey> getServiceRunningStatus();
	
}
