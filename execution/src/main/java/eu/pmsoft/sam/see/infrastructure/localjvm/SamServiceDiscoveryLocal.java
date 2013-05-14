package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.see.api.infrastructure.SamServiceDiscovery;
import eu.pmsoft.see.api.model.SIURL;

import java.util.Map;

public class SamServiceDiscoveryLocal implements SamServiceDiscovery {

    private final Map<SIURL, ServiceKeyDeprecated> running = Maps.newHashMap();

    @Override
    public void serviceTransactionCreated(SIURL url, ServiceKeyDeprecated contract) {
        running.put(url, contract);
        // TODO service discovery ogloszenie
    }

    @Override
    public Map<SIURL, ServiceKeyDeprecated> getServiceRunningStatus() {
        return ImmutableMap.copyOf(running);
    }

}
