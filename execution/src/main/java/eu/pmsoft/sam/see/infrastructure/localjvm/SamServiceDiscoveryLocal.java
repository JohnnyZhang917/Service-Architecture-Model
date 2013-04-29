package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.api.infrastructure.SamServiceDiscovery;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.model.STID;

import java.util.Map;

public class SamServiceDiscoveryLocal implements SamServiceDiscovery {

    private final Map<SIURL, ServiceKey> running = Maps.newHashMap();

    @Override
    public void serviceTransactionCreated(SIURL url, ServiceKey contract) {
        running.put(url, contract);
        // TODO service discovery ogloszenie
    }

    @Override
    public Map<SIURL, ServiceKey> getServiceRunningStatus() {
        return ImmutableMap.copyOf(running);
    }

}
