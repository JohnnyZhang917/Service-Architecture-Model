package pmsoft.sam.see.api.plugin;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIURL;

public interface SamServiceDiscoveryListener {

	public void serviceInstanceCreated(SIURL url, ServiceKey contract);
	
}
