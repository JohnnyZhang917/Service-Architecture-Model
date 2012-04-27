package pmsoft.sam.module.serviceRegistry.local;

import pmsoft.sam.module.serviceRegistry.SamServiceRegistry;

import com.google.inject.PrivateModule;

public class LocalVMServiceRegistryModule extends PrivateModule {

	@Override
	protected void configure() {
		bind(SamServiceRegistry.class).to(LocalVMServiceRegistry.class).asEagerSingleton();
		bind(ServiceRegistryModel.class);
		expose(SamServiceRegistry.class);
	}

}
