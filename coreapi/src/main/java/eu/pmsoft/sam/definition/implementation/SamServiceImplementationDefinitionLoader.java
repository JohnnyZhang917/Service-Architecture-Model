package eu.pmsoft.sam.definition.implementation;

import com.google.inject.Module;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;

public interface SamServiceImplementationDefinitionLoader {

    public ContractAndModule signature(Class<? extends SamServiceDefinition> contract,Class<? extends Module> serviceImplementationModule);

    interface ContractAndModule {
        public ContractAndModule withBindingsTo(Class<? extends SamServiceDefinition> userService);
    }


}
