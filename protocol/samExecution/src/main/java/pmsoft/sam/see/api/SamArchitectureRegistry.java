package pmsoft.sam.see.api;

import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;

public interface SamArchitectureRegistry {

    SamService getService(ServiceKey serviceKey);

}
