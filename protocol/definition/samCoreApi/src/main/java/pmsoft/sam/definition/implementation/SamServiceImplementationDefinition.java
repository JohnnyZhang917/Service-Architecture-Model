package pmsoft.sam.definition.implementation;

import pmsoft.sam.definition.service.SamServiceDefinition;

public interface SamServiceImplementationDefinition<T extends SamServiceDefinition> {

    public void loadServiceImplementationDefinition(SamServiceImplementationLoader loader);
}
