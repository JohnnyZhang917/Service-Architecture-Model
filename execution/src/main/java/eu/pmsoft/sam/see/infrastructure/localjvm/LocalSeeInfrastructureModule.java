package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.pmsoft.sam.see.api.infrastructure.SamArchitectureManagement;
import eu.pmsoft.sam.see.api.infrastructure.SamArchitectureRegistry;
import eu.pmsoft.sam.see.api.infrastructure.SamServiceDiscovery;
import eu.pmsoft.sam.see.api.infrastructure.SamServiceRegistry;

public class LocalSeeInfrastructureModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(SamArchitectureManagement.class).to(SamArchitectureRegistryLocal.class).asEagerSingleton();
        bind(SamServiceDiscovery.class).to(SamServiceDiscoveryLocal.class).asEagerSingleton();
        bind(SamServiceRegistry.class).to(SamServiceRegistryLocal.class).asEagerSingleton();

//        expose(SamArchitectureRegistry.class);
//        expose(SamArchitectureManagement.class);
//        expose(SamServiceDiscovery.class);
//        expose(SamServiceRegistry.class);
    }

    @Provides
    public SamArchitectureRegistry provideArchitectureManager(SamArchitectureManagement manager) {
        return manager;
    }

}
