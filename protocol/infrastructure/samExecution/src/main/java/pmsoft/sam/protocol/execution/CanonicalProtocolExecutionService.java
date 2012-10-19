package pmsoft.sam.protocol.execution;

import pmsoft.sam.protocol.injection.internal.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.injection.internal.ServiceExecutionRequest;

public interface CanonicalProtocolExecutionService {

	public CanonicalProtocolRequestData executeCanonicalCalls(ServiceExecutionRequest request);

}
