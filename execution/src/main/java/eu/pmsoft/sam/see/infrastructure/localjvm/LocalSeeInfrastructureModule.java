package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.pmsoft.sam.see.api.SamArchitectureManagement;
import eu.pmsoft.sam.see.api.SamArchitectureRegistry;
import eu.pmsoft.sam.see.api.SamServiceDiscovery;

public class LocalSeeInfrastructureModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(SamArchitectureManagement.class).to(SamArchitectureRegistryLocal.class).asEagerSingleton();
        bind(SamServiceDiscovery.class).to(SamServiceDiscoveryLocal.class).asEagerSingleton();
//        expose(SamArchitectureRegistry.class);
//        expose(SamArchitectureManagement.class);
//        expose(SamServiceDiscovery.class);
    }

    @Provides
    public SamArchitectureRegistry provideArchitectureManager(SamArchitectureManagement manager) {
        return manager;
    }

}
