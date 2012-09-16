package pmsoft.sam.definition.instance;

import pmsoft.sam.definition.service.SamServiceDefinition;

import com.google.inject.Module;

public interface SamServiceImplementationContractLoader {

	void loadImplementationContract(Class<? extends SamServiceDefinition> serviceContract,
			Class<? extends Module> serviceImplementationModule, Class<? extends SamServiceDefinition>[] bindedServices);

}
