package pmsoft.sam.definition.instance;

/**
 * Interface for the Service Implementation contracts.
 * 
 * On execution, Contract information is passed to a loader.
 * 
 * @author pawel
 * 
 */
public interface SamServiceImplementationContract {
	
	void readContract(SamServiceImplementationContractLoader reader);

}
