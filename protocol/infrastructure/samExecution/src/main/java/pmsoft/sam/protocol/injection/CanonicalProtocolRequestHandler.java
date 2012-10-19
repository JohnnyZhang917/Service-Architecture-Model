package pmsoft.sam.protocol.injection;

import pmsoft.sam.protocol.injection.internal.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.injection.internal.ServiceExecutionRequest;

public interface CanonicalProtocolRequestHandler {

	CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request);
}
