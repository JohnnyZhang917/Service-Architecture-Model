package eu.pmsoft.see.api.infrastructure;

import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.see.api.model.SIURL;

import java.util.Map;

public interface SamServiceDiscovery {

    public void serviceTransactionCreated(SIURL url, ServiceKeyDeprecated contract);

    public Map<SIURL, ServiceKeyDeprecated> getServiceRunningStatus();

}
