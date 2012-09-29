package pmsoft.sam.module.serviceRegistry.local;

import pmsoft.sam.module.see.serviceRegistry.SamServiceRegistryDeprecated;

import com.google.inject.PrivateModule;

public class LocalVMServiceRegistryModule extends PrivateModule {

	@Override
	protected void configure() {
		bind(SamServiceRegistryDeprecated.class).to(LocalVMServiceRegistry.class).asEagerSingleton();
		bind(ServiceRegistryModel.class);
		expose(SamServiceRegistryDeprecated.class);
	}

}
