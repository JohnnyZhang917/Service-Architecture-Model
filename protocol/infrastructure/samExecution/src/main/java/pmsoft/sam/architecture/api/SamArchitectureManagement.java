package pmsoft.sam.architecture.api;

import pmsoft.sam.architecture.model.SamArchitecture;

public interface SamArchitectureManagement extends SamArchitectureRegistry {

	public void registerArchitecture(SamArchitecture architecture);
	
}
