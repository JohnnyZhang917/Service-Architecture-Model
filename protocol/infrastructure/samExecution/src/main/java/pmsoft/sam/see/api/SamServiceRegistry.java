package pmsoft.sam.see.api;

import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import pmsoft.sam.see.api.model.SamServiceImplementation;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;

public interface SamServiceRegistry {

	public void registerServiceImplementationPackage(SamServiceImplementationPackageContract definition);
	
	public SamServiceImplementation getImplementation(SamServiceImplementationKey key);
	
}
