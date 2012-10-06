package pmsoft.sam.canonical.deprecated.service;

import pmsoft.sam.canonical.deprecated.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.deprecated.model.ServiceExecutionRequest;

public interface CanonicalProtocolExecutionService {

	public CanonicalProtocolRequestData executeCanonicalCalls(ServiceExecutionRequest request);

}
