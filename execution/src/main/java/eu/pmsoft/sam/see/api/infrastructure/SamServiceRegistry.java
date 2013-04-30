package eu.pmsoft.sam.see.api.infrastructure;

import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.see.api.model.SamServiceImplementation;
import eu.pmsoft.sam.see.api.model.SamServiceImplementationKey;

@Deprecated
public interface SamServiceRegistry {

    /**
     * Register Package of implementations
     *
     * @param definition
     */
    @Deprecated
    public void registerServiceImplementationPackage(SamServiceImplementationPackageContract definition);

    /**
     * Get information about a specific implementations. The implementation must be already registered
     *
     * @param key Key identifying the implementation
     * @return implementation by key
     */
    @Deprecated
    public SamServiceImplementation getImplementation(SamServiceImplementationKey key);


}
