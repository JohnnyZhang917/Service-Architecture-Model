package eu.pmsoft.sam.see.api.infrastructure;

import eu.pmsoft.sam.architecture.model.SamService;
import eu.pmsoft.sam.architecture.model.ServiceKey;

public interface SamArchitectureRegistry {

    SamService getService(ServiceKey serviceKey);

}
