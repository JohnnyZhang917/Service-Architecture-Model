package pmsoft.sam.definition.implementation;

import pmsoft.sam.definition.service.SamServiceDefinition;

import com.google.inject.Module;

public interface SamServiceImplementationContractLoader {
	// Regular grammar loader interface
	// ServicePackage: ServiceImplementation*
	// ServiceImplementation: Contract ExtrenalContract* Module
	//
	// Key for ServiceImplementation is Module.
	
	public SamServiceImplementationGrammarContract provideContract(Class<? extends SamServiceDefinition> serviceContract); 

	static public interface SamServiceImplementationGrammarContract {
		public SamServiceImplementationGrammarContract withBindingsTo(Class<? extends SamServiceDefinition> bindedService); 
		
		public void implementedInModule(Class<? extends Module> serviceImplementationModule);
	}

	
	
}
