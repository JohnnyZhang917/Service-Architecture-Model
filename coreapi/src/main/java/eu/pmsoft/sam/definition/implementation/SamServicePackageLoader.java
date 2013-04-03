package eu.pmsoft.sam.definition.implementation;

import eu.pmsoft.sam.definition.service.SamServiceDefinition;

public interface SamServicePackageLoader {

    public void registerImplementation(AbstractSamServiceImplementationDefinition<? extends SamServiceDefinition> serviceImplementationContract);

}
