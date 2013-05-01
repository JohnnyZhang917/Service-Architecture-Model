package eu.pmsoft.sam.see.api.infrastructure;

import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.see.api.model.SamServiceImplementationDeprecated;
import eu.pmsoft.sam.see.api.model.SamServiceImplementationKey;


public interface SamServiceRegistryDeprecated {

    /**
     * Register Package of implementations
     *
     * @param definition
     */
    public void registerServiceImplementationPackage(SamServiceImplementationPackageContract definition);

    /**
     * Get information about a specific implementations. The implementation must be already registered
     *
     * @param key Key identifying the implementation
     * @return implementation by key
     */
    public SamServiceImplementationDeprecated getImplementation(SamServiceImplementationKey key);


}
