package pmsoft.sam.module.see;

import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.model.ServiceExecutionRequest;

public interface ExternalServiceReference {

	CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request);

}
