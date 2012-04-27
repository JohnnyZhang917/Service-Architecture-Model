package pmsoft.sam.module.model;

import pmsoft.sam.canonical.model.ServiceExecutionRequest;
import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;

public interface ExternalServiceReference {

	CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request);

}
