package pmsoft.sam.module.definition.implementation;

import pmsoft.sam.module.definition.implementation.grammar.SamServiceImplementationLoader;

public interface SamServiceImplementationDefinition {

	void loadOn(SamServiceImplementationLoader loader);
	
}
