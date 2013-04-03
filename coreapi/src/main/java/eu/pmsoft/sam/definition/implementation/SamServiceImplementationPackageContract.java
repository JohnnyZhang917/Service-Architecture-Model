package eu.pmsoft.sam.definition.implementation;

/**
 * Interface for the Service Implementation contracts.
 * <p/>
 * On execution, Contract information is passed to a loader.
 *
 * @author pawel
 */
public interface SamServiceImplementationPackageContract {
    void loadContractPackage(SamServicePackageLoader reader);
}