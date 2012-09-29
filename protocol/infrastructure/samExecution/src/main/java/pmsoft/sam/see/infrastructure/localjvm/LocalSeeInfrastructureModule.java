package pmsoft.sam.see.infrastructure.localjvm;

import pmsoft.sam.architecture.api.SamArchitectureManagement;
import pmsoft.sam.architecture.api.SamArchitectureRegistry;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class LocalSeeInfrastructureModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SamArchitectureManagement.class).to(SamArchitectureRegistryLocal.class).asEagerSingleton();
//		expose(SamArchitectureRegistry.class);
	}

	@Provides
	public SamArchitectureRegistry provideArchitectureManager(SamArchitectureManagement manager) {
		return manager;
	}

}
