package pmsoft.sam.module.see;

import pmsoft.sam.canonical.deprecated.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.deprecated.model.ServiceExecutionRequest;

public interface ExternalServiceReference {

	CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request);

}
