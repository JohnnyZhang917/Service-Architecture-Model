package pmsoft.sam.see.infrastructure.localjvm;

import java.util.List;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.SamServiceDiscovery;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.plugin.SamServiceDiscoveryListener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

public class SamServiceDiscoveryLocal implements SamServiceDiscovery {

	private final ImmutableSet<SamServiceDiscoveryListener> listeners;

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
		for (SamServiceDiscoveryListener listener : listeners) {
			listener.serviceInstanceCreated(url, contract);
		}
	}

}
