package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.api.SamServiceDiscovery;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.plugin.SamServiceDiscoveryListener;

import java.util.List;
import java.util.Map;

public class SamServiceDiscoveryLocal implements SamServiceDiscovery {

    private final ImmutableSet<SamServiceDiscoveryListener> listeners;
    private final Map<SIURL, ServiceKey> running = Maps.newHashMap();

    @Inject
    public SamServiceDiscoveryLocal(Injector infrastructureInjector) {
        super();
        List<Binding<SamServiceDiscoveryListener>> plugInListeners = infrastructureInjector.findBindingsByType(TypeLiteral
                .get(SamServiceDiscoveryListener.class));
        Builder<SamServiceDiscoveryListener> builder = ImmutableSet.builder();
        for (Binding<SamServiceDiscoveryListener> plugin : plugInListeners) {
            builder.add(plugin.getProvider().get());
        }
        listeners = builder.build();
    }

    @Override
    public void serviceTransactionCreated(SIURL url, ServiceKey contract) {
        running.put(url, contract);
        for (SamServiceDiscoveryListener listener : listeners) {
            listener.serviceInstanceCreated(url, contract);
        }
    }

    @Override
    public Map<SIURL, ServiceKey> getServiceRunningStatus() {
        return ImmutableMap.copyOf(running);
    }

}
