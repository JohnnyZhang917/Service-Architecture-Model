package pmsoft.sam.see.infrastructure.localjvm;

import pmsoft.sam.see.api.SamArchitectureManagement;
import pmsoft.sam.see.api.SamArchitectureRegistry;
import pmsoft.sam.see.api.SamServiceDiscovery;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;

public class LocalSeeInfrastructureModule extends PrivateModule {

	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		bind(SamArchitectureManagement.class).to(SamArchitectureRegistryLocal.class).asEagerSingleton();
		expose(SamArchitectureRegistry.class);
		expose(SamArchitectureManagement.class);
		bind(SamServiceDiscovery.class).to(SamServiceDiscoveryLocal.class).asEagerSingleton();
		expose(SamServiceDiscovery.class);
	}

	@Provides
	public SamArchitectureRegistry provideArchitectureManager(SamArchitectureManagement manager) {
		return manager;
	}

}
