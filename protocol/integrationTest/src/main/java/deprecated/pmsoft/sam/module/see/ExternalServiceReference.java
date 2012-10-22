package deprecated.pmsoft.sam.module.see;

import pmsoft.sam.protocol.injection.internal.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.injection.internal.ServiceExecutionRequest;

public interface ExternalServiceReference {

	CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request);

}
