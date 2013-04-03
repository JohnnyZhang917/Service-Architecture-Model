package pmsoft.sam.definition.implementation;

import com.google.inject.Module;
import pmsoft.sam.definition.service.SamServiceDefinition;

public interface SamServiceImplementationLoader {

    public InternalServiceImplementationDefinition provideContract(Class<? extends SamServiceDefinition> serviceDefinition);

    interface InternalServiceImplementationDefinition {
        public void withBindingsTo(Class<? extends SamServiceDefinition> userService);

        public void implementedInModule(Class<? extends Module> serviceImplementationModule);
    }


}
