package pmsoft.sam.canonical.api;

import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.model.ServiceExecutionRequest;

public interface CanonicalProtocolRequestHandler extends MethodRecordContext {

	CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request);
}
