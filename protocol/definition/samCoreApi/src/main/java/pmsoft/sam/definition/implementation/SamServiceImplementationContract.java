package pmsoft.sam.definition.implementation;

import com.google.inject.Module;
import pmsoft.sam.definition.service.SamServiceDefinition;

public interface SamServiceImplementationContract<T extends SamServiceDefinition> {
    // ServiceImplementation: Contract ExtrenalContract* Module
    //
    // Key for ServiceImplementation is Module.

    public SamServiceImplementationContract<T> provideContract(Class<T> serviceContract);
    public SamServiceImplementationContract<T> withBindingsTo(Class<? extends SamServiceDefinition> bindedService);
    public void implementedInModule(Class<? extends Module> serviceImplementationModule);

}
