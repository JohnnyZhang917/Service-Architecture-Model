package eu.pmsoft.sam.see.api.infrastructure;

import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.model.STID;

import java.util.Map;

public interface SamServiceDiscovery {

    public void serviceTransactionCreated(SIURL url, ServiceKey contract);

    public Map<SIURL, ServiceKey> getServiceRunningStatus();

}
