package pmsoft.sam.canonical.deprecated.api;

import pmsoft.sam.canonical.deprecated.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.deprecated.model.ServiceExecutionRequest;

public interface CanonicalProtocolRequestHandler extends MethodRecordContext {

	CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request);
}
