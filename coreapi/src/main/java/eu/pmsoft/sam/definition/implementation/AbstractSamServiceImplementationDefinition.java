package eu.pmsoft.sam.definition.implementation;

import com.google.inject.Module;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;

public abstract class AbstractSamServiceImplementationDefinition<T extends SamServiceDefinition> implements SamServiceImplementationDefinition<T> {

    private final Class<T> serviceContract;
    private SamServiceImplementationLoader.InternalServiceImplementationDefinition definitionLoader;

    protected AbstractSamServiceImplementationDefinition(Class<T> serviceContract) {
        this.serviceContract = serviceContract;
    }

    @Override
    public final void loadServiceImplementationDefinition(SamServiceImplementationLoader loader) {
        try {
            this.definitionLoader = loader.provideContract(serviceContract);
            implementationDefinition();
        } finally {
            this.definitionLoader = null;
        }
    }

    protected abstract void implementationDefinition();

    protected final void withBindingsTo(Class<? extends SamServiceDefinition> userService) {
        definitionLoader.withBindingsTo(userService);
    }

    protected final void implementedInModule(Class<? extends Module> serviceImplementationModule) {
        definitionLoader.implementedInModule(serviceImplementationModule);
    }

}
