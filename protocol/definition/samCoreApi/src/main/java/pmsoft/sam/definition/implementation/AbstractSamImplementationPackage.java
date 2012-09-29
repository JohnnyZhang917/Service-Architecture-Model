package pmsoft.sam.definition.implementation;

import pmsoft.sam.definition.implementation.SamServiceImplementationContractLoader.SamServiceImplementationGrammarContract;
import pmsoft.sam.definition.service.SamServiceDefinition;

/**
 * Definition of many implementations of services.
 * 
 * @author pawel
 * 
 */
public abstract class AbstractSamImplementationPackage implements
		SamServiceImplementationPackageContract {

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

	protected final SamServiceImplementationGrammarContract provideContract(
			Class<? extends SamServiceDefinition> serviceContract) {
		return reader.provideContract(serviceContract);
	}

}
