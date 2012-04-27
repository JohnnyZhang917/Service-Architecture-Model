package pmsoft.sam.module.definition.implementation;


import pmsoft.sam.module.definition.architecture.SamServiceDefinition;
import pmsoft.sam.module.model.ServiceKey;

public interface FinalPrototypeDeclarationStatement {

	public void done();
	
	// FIXME implement the category access relation
	public FinalPrototypeDeclarationStatement accessTo(ServiceKey... externals);
	
	public FinalPrototypeDeclarationStatement accessTo(String... externals);
	
	public FinalPrototypeDeclarationStatement accessTo(Class<? extends SamServiceDefinition> externals);
	
}
