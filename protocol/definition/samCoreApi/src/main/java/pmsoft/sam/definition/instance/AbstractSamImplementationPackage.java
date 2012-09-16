package pmsoft.sam.definition.instance;

import pmsoft.sam.definition.service.SamServiceDefinition;

import com.google.inject.Module;

public abstract class AbstractSamImplementationPackage implements SamServiceImplementationContract {

	private SamServiceImplementationContractLoader reader;

	@Override
	public void readContract(SamServiceImplementationContractLoader reader) {
		// TODO try
		this.reader = reader;
		implementationDefinition();
		this.reader = null;

	}

	public abstract void implementationDefinition();

	protected void addImplementationContract(Class<? extends SamServiceDefinition> serviceContract, Class<? extends Module> serviceImplementationModule,
			Class<? extends SamServiceDefinition>[] bindedServices) {
		reader.loadImplementationContract(serviceContract, serviceImplementationModule, bindedServices);
	}

}
