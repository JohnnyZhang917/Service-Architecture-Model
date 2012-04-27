package pmsoft.sam.canonical.service;

import pmsoft.sam.canonical.model.ServiceExecutionRequest;
import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;

public interface CanonicalProtocolExecutionService {

	public CanonicalProtocolRequestData executeCanonicalCalls(ServiceExecutionRequest request);

}
