package pmsoft.sam.module.serviceRegistry.local;

import java.util.Collection;
import java.util.List;

import pmsoft.sam.module.definition.implementation.SamServiceImplementationDefinition;
import pmsoft.sam.module.definition.implementation.grammar.SamServiceImplementationLoader;
import pmsoft.sam.module.model.ServiceImplementation;
import pmsoft.sam.module.model.ServiceImplementationKey;
import pmsoft.sam.module.model.ServiceKey;
import pmsoft.sam.module.serviceRegistry.SamServiceRegistry;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Module;

public class LocalVMServiceRegistry implements SamServiceRegistry {

	private final ServiceRegistryModel model;
	
	@Inject
	public LocalVMServiceRegistry(ServiceRegistryModel model) {
		super();
		this.model = model;
	}

	public void registerServiceImplementation(SamServiceImplementationDefinition definition) {
		definition.loadOn(new SamServiceImplementationLoader() {
			public void registerImplementation(ServiceKey serviceSpecification, Class<? extends Module> implementationModule,
					List<ServiceKey> withAccessToServices) {
				model.registerImplementation(serviceSpecification, implementationModule, withAccessToServices);
			}
		});
	}

	public void unRegisterServiceImplementation(SamServiceImplementationDefinition definition) {
		definition.loadOn(new SamServiceImplementationLoader() {
			public void registerImplementation(ServiceKey serviceSpecification, Class<? extends Module> implementationModule,
					List<ServiceKey> withAccessToServices) {
				model.unregisterImplementation(serviceSpecification,implementationModule);
			}
		});
	}

	public List<ServiceImplementationKey> getImplementationsForSpecification(ServiceKey key) {
		Collection<ServiceImplementationKey> implems = model.getImplementations().get(key);
		return ImmutableList.copyOf(implems);
	}

	public ServiceImplementation getImplementation(ServiceImplementationKey implementationKey) {
		return model.getImplementationModel(implementationKey);
	}

}
