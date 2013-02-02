package pmsoft.sam.definition.implementation;

import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.definition.service.SamServiceLoader;

public interface SamServiceImplementationDefinition<T extends SamServiceDefinition> {

    public void loadServiceImplementationDefinition(SamServiceImplementationLoader loader);
}
