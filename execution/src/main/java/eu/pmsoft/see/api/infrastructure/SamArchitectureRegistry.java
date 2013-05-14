package eu.pmsoft.see.api.infrastructure;

import eu.pmsoft.sam.architecture.model.SamServiceDeprecated;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;

public interface SamArchitectureRegistry {

    SamServiceDeprecated getService(ServiceKeyDeprecated serviceKeyDeprecated);

}
