package pmsoft.sam.module.serviceRegistry.local;

import java.util.Collection;
import java.util.List;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.implementation.SamServiceImplementationContract;
import pmsoft.sam.module.see.serviceRegistry.SamServiceRegistryDeprecated;
import pmsoft.sam.see.api.model.ServiceImplementation;
import pmsoft.sam.see.api.model.ServiceImplementationKey;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class LocalVMServiceRegistry implements SamServiceRegistryDeprecated {

	private final ServiceRegistryModel model;
	
	@Inject
	public LocalVMServiceRegistry(ServiceRegistryModel model) {
		super();
		this.model = model;
	}

	public void registerServiceImplementation(SamServiceImplementationContract definition) {
		// TODO
//		definition.loadOn(new SamServiceImplementationLoader() {
//			public void registerImplementation(ServiceKey serviceSpecification, Class<? extends Module> implementationModule,
//					List<ServiceKey> withAccessToServices) {
//				model.registerImplementation(serviceSpecification, implementationModule, withAccessToServices);
//			}
//		});
	}

	public void unRegisterServiceImplementation(SamServiceImplementationContract definition) {
//		definition.loadOn(new SamServiceImplementationLoader() {
//			public void registerImplementation(ServiceKey serviceSpecification, Class<? extends Module> implementationModule,
//					List<ServiceKey> withAccessToServices) {
//				model.unregisterImplementation(serviceSpecification,implementationModule);
//			}
//		});
	}

	public List<ServiceImplementationKey> getImplementationsForSpecification(ServiceKey key) {
		Collection<ServiceImplementationKey> implems = model.getImplementations().get(key);
		return ImmutableList.copyOf(implems);
	}

	public ServiceImplementation getImplementation(ServiceImplementationKey implementationKey) {
		return model.getImplementationModel(implementationKey);
	}

}
