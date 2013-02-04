package eu.pmsoft.sam.definition.implementation;

import com.google.inject.Module;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;

public abstract class AbstractSamServiceImplementationDefinition<T extends SamServiceDefinition> implements SamServiceImplementationDefinition<T> {

    private final Class<T> serviceContract;
    private final Class<? extends Module> implementationModule;
    private SamServiceImplementationDefinitionLoader.ContractAndModule definitionLoader;

    protected AbstractSamServiceImplementationDefinition(Class<T> serviceContract, Class<? extends Module> implementationModule) {
        this.serviceContract = serviceContract;
        this.implementationModule = implementationModule;
    }

    @Override
    public final void loadServiceImplementationDefinition(SamServiceImplementationDefinitionLoader loader) {
        try {
            this.definitionLoader = loader.signature(serviceContract, implementationModule);
            implementationDefinition();
        } finally {
            this.definitionLoader = null;
        }
    }

    protected void implementationDefinition() {

    }

    protected final void withBindingsTo(Class<? extends SamServiceDefinition> userService) {
        definitionLoader.withBindingsTo(userService);
    }

}
