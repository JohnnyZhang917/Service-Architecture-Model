package deprecated.pmsoft.sam.module.serviceRegistry.local;


import com.google.inject.PrivateModule;

import deprecated.pmsoft.sam.module.see.serviceRegistry.SamServiceRegistryDeprecated;

public class LocalVMServiceRegistryModule extends PrivateModule {

	@Override
	protected void configure() {
		bind(SamServiceRegistryDeprecated.class).to(LocalVMServiceRegistry.class).asEagerSingleton();
		bind(ServiceRegistryModel.class);
		expose(SamServiceRegistryDeprecated.class);
	}

}
