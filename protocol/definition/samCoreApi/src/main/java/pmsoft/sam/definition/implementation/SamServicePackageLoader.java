package pmsoft.sam.definition.implementation;

import pmsoft.sam.definition.service.SamServiceDefinition;

public interface SamServicePackageLoader {

    public void registerImplementation(AbstractSamServiceImplementationDefinition<? extends SamServiceDefinition> serviceImplementationContract);

}
