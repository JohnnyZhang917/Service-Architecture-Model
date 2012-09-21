package pmsoft.sam.definition.instance;

import pmsoft.sam.definition.instance.SamServiceImplementationContractLoader.SamServiceInstanceGrammarContract;
import pmsoft.sam.definition.service.SamServiceDefinition;

/**
 * Definition of many implementations of services.
 * 
 * @author pawel
 * 
 */
public abstract class AbstractSamImplementationPackage implements
		SamServiceImplementationContract {

	private SamServiceImplementationContractLoader reader;

	@Override
	public void loadContractPackage(SamServiceImplementationContractLoader reader) {
		try {
			this.reader = reader;
			implementationDefinition();
		} finally {
			this.reader = null;
		}

	}

	public abstract void implementationDefinition();

	protected final SamServiceInstanceGrammarContract provideContract(
			Class<? extends SamServiceDefinition> serviceContract) {
		return reader.provideContract(serviceContract);
	}

}
