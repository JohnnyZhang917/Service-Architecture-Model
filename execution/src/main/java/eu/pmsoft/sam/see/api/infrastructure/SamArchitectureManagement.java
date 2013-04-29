package eu.pmsoft.sam.see.api.infrastructure;

import eu.pmsoft.sam.architecture.model.SamArchitecture;

public interface SamArchitectureManagement extends SamArchitectureRegistry {

    public void registerArchitecture(SamArchitecture architecture);

}
