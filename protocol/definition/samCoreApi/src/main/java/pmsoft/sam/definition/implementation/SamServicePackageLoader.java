package pmsoft.sam.definition.implementation;

import pmsoft.sam.definition.service.SamServiceDefinition;

import com.google.inject.Module;

public interface SamServicePackageLoader {

    public void registerImplementation(AbstractSamServiceImplementationDefinition<? extends SamServiceDefinition> serviceImplementationContract);

}
