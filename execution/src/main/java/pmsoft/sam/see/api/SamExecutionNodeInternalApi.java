package pmsoft.sam.see.api;

import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SamServiceInstance;

public interface SamExecutionNodeInternalApi extends SamExecutionNode {

    public SamServiceInstance getInternalServiceInstance(SIID exposedService);

}
