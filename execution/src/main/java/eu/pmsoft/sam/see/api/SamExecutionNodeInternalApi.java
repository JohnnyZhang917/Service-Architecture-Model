package eu.pmsoft.sam.see.api;

import eu.pmsoft.sam.see.api.model.SIID;
import eu.pmsoft.sam.see.api.model.SamServiceInstance;

public interface SamExecutionNodeInternalApi extends SamExecutionNode {

    public SamServiceInstance getInternalServiceInstance(SIID exposedService);

}
