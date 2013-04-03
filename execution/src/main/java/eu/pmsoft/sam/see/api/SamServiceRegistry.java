package eu.pmsoft.sam.see.api;

import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.model.SamInstanceTransaction;
import eu.pmsoft.sam.see.api.model.SamServiceImplementation;
import eu.pmsoft.sam.see.api.model.SamServiceImplementationKey;

public interface SamServiceRegistry {

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
    public SamServiceImplementation getImplementation(SamServiceImplementationKey key);

    /**
     * Get a currently running transaction
     *
     * @param url SIURL identifying this transaction
     * @return transaction by url
     */
    public SamInstanceTransaction getTransaction(SIURL url);

}
