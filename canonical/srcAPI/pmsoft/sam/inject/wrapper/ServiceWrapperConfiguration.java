package pmsoft.sam.inject.wrapper;

import java.util.List;

import pmsoft.sam.inject.free.ExternalInstanceProvider;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Injector;
import com.google.inject.Key;

public class ServiceWrapperConfiguration implements ExternalInstanceProvider {

	private class InjectorBindingInstanceProvider implements ExternalInstanceProvider {
		private final InstanceProvider finalProvider;
		private final Table<Key<?>, Long, Object> instanceTable = HashBasedTable.create();

		public InjectorBindingInstanceProvider(InstanceProvider finalProvider) {
			this.finalProvider = finalProvider;
		}

		@SuppressWarnings("unchecked")
		public <T> T getReference(Key<T> key, long instanceReferenceNr, int serviceSlotNr) {
			Object instanceRef = instanceTable.get(key, instanceReferenceNr);
			if (instanceRef == null) {
				instanceRef = finalProvider.getInstance(key);
				instanceTable.put(key, instanceReferenceNr, instanceRef);
			}
			return (T) instanceRef;
		}

	}

	private final List<ExternalInstanceProvider> providerPerSlot = Lists.newArrayList();

	public void registerServiceInstance(ServiceBindingDefinition serviceInstanceDef,
			WrappingInjectorControllerInternal contextController) {
		ExternalInstanceProvider serviceProvider;

		if (serviceInstanceDef.getRealServiceInjector() != null) {
			final Injector finalInjector = serviceInstanceDef.getRealServiceInjector();
			serviceProvider = new InjectorBindingInstanceProvider(new InstanceProvider() {
				public <T> T getInstance(Key<T> key) {
					return finalInjector.getInstance(key);
				}
			});
		} else {
			InstanceProvider externalInjector = contextController.getExternalInstanceProvider(
					serviceInstanceDef.getExternalInstanceURL(), serviceInstanceDef.getServiceKeys());
			serviceProvider = new InjectorBindingInstanceProvider(externalInjector);
		}
		providerPerSlot.add(serviceProvider);
	}

	public <T> T getReference(Key<T> key, long instanceReferenceNr, int serviceSlotNr) {
		return providerPerSlot.get(serviceSlotNr).getReference(key, instanceReferenceNr, -1);
	}

}
