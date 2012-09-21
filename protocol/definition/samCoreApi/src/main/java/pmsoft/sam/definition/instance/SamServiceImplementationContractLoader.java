package pmsoft.sam.definition.instance;

import pmsoft.sam.definition.service.SamServiceDefinition;

import com.google.inject.Module;

public interface SamServiceImplementationContractLoader {
	// Regular grammar loader interface
	// ServicePackage: ServiceInstance*
	// ServiceInstance: Contract ExtrenalContract* Module
	
	public SamServiceInstanceGrammarContract provideContract(Class<? extends SamServiceDefinition> serviceContract); 

	static public interface SamServiceInstanceGrammarContract {
		public SamServiceInstanceGrammarContract withBindingsTo(Class<? extends SamServiceDefinition> bindedService); 
		
		public void implementedInModule(Class<? extends Module> serviceImplementationModule);
	}

	
	
}
