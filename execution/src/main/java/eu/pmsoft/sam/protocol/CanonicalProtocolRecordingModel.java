package eu.pmsoft.sam.protocol;

import eu.pmsoft.see.api.model.SamServiceInstanceTransaction;
import eu.pmsoft.see.api.setup.SamExecutionNodeInternalApi;

import java.util.UUID;

public interface CanonicalProtocolRecordingModel {

    public CanonicalProtocolThreadExecutionContext createExecutionContext(final SamServiceInstanceTransaction functionContract, SamExecutionNodeInternalApi samExecutionNodeJVM);

    public CanonicalProtocolThreadExecutionContext bindExecutionContext(SamServiceInstanceTransaction transaction, UUID transactionUniqueId, SamExecutionNodeInternalApi samExecutionNodeJVM);

}
