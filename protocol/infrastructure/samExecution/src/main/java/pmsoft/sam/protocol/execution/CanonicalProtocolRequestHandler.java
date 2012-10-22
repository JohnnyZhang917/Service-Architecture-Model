package pmsoft.sam.protocol.execution;

import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;

public interface CanonicalProtocolRequestHandler {

	CanonicalProtocolRequestData handleRequest(CanonicalProtocolRequest request);
}
