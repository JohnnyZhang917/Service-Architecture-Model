package pmsoft.sam.module.serviceRegistry.local;

import java.util.List;
import java.util.Map;

import pmsoft.sam.module.model.ServiceImplementation;
import pmsoft.sam.module.model.ServiceImplementationKey;
import pmsoft.sam.module.model.ServiceKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Module;

public class ServiceRegistryModel {

	private final Multimap<ServiceKey, ServiceImplementationKey> implementations = HashMultimap.create();

	private final Map<ServiceImplementationKey, ServiceImplementationModel> models = Maps.newHashMap();

	public Multimap<ServiceKey, ServiceImplementationKey> getImplementations() {
		return implementations;
	}

	public ServiceImplementation getImplementationModel(ServiceImplementationKey key) {
		return models.get(key);
	}

	public void registerImplementation(ServiceKey serviceSpecification, Class<? extends Module> implementationModule,
			List<ServiceKey> withAccessToServices) {
		// FIXME check that provided module match the service specification
		ServiceImplementationKey implementationKey = new ServiceImplementationKey(
				serviceSpecification.getServiceDefinitionSignature(), implementationModule.getCanonicalName());
		Preconditions.checkState(!implementations.containsEntry(serviceSpecification, implementationKey),
				"Implementation already registered %s ", implementationKey);
		implementations.put(serviceSpecification, implementationKey);
		ServiceImplementationModel model = new ServiceImplementationModel(serviceSpecification, implementationKey,
				implementationModule, ImmutableList.copyOf(withAccessToServices));
		models.put(implementationKey, model);

	}

	public void unregisterImplementation(ServiceKey serviceSpecification, Class<? extends Module> implementationModule) {
		// FIXME integrate with SEE to notify/kill existing service instance for
		// this implementation
		ServiceImplementationKey implementationKey = new ServiceImplementationKey(
				serviceSpecification.getServiceDefinitionSignature(), implementationModule.getCanonicalName());
		Preconditions.checkState(implementations.containsEntry(serviceSpecification, implementationKey));
		implementations.remove(serviceSpecification, implementationKey);
		models.remove(implementationKey);
	}

}
