package eu.pmsoft.sam.see.api.infrastructure;

import eu.pmsoft.sam.architecture.model.SamServiceDeprecated;
import eu.pmsoft.sam.architecture.model.ServiceKey;

public interface SamArchitectureRegistry {

    SamServiceDeprecated getService(ServiceKey serviceKey);

}
