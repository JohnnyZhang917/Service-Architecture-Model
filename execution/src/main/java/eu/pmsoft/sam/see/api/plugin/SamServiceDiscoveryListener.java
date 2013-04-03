package eu.pmsoft.sam.see.api.plugin;

import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.api.model.SIURL;

public interface SamServiceDiscoveryListener {

    public void serviceInstanceCreated(SIURL url, ServiceKey contract);

}
