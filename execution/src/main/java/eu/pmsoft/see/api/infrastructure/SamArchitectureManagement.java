package eu.pmsoft.see.api.infrastructure;

import eu.pmsoft.sam.architecture.model.SamArchitectureDeprecated;

public interface SamArchitectureManagement extends SamArchitectureRegistry {

    public void registerArchitecture(SamArchitectureDeprecated architecture);

}
