package pmsoft.sam.protocol.injection;

import java.util.List;

import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Key;

public class InstanceProviderTransactionContext implements ExternalInstanceProvider {

	private static class InjectorBindingInstanceProvider {
		private final InstanceProvider finalProvider;
		private final Table<Key<?>, Long, Object> instanceTable = HashBasedTable.create();

		public InjectorBindingInstanceProvider(InstanceProvider finalProvider) {
			this.finalProvider = finalProvider;
		}

		@SuppressWarnings("unchecked")
		public <T> T getReference(Key<T> key, long instanceReferenceNr) {
			Object instanceRef = instanceTable.get(key, instanceReferenceNr);
			if (instanceRef == null) {
				instanceRef = finalProvider.getInstance(key);
				instanceTable.put(key, instanceReferenceNr, instanceRef);
			}
			return (T) instanceRef;
		}

	}

	private final InjectorBindingInstanceProvider[] providerPerSlot;

	public InstanceProviderTransactionContext(List<InstanceProvider> serviceInjector) {
		Preconditions.checkNotNull(serviceInjector);
		Preconditions.checkArgument(serviceInjector.size() > 0, "Empty list of service injectors.");
		this.providerPerSlot = new InjectorBindingInstanceProvider[serviceInjector.size()];
		for (int i = 0; i < serviceInjector.size(); i++) {
			providerPerSlot[i] = new InjectorBindingInstanceProvider(serviceInjector.get(i));
		}
	}

	public <T> T getReference(Key<T> key, long instanceReferenceNr, int serviceSlotNr) {
		assert(providerPerSlot[serviceSlotNr] != null);
		return providerPerSlot[serviceSlotNr].getReference(key, instanceReferenceNr);
	}

}
