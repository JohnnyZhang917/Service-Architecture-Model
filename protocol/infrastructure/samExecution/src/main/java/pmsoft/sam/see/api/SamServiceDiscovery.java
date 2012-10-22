package pmsoft.sam.see.api;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIURL;


public interface SamServiceDiscovery {

	public void serviceTransactionCreated(SIURL url, ServiceKey contract);
}
