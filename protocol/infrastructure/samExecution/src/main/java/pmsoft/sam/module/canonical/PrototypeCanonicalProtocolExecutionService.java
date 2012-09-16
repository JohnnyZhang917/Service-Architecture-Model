package pmsoft.sam.module.canonical;

import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.model.ServiceExecutionRequest;
import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;
import pmsoft.sam.module.see.ExternalServiceReference;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;

import com.google.inject.Inject;

public class PrototypeCanonicalProtocolExecutionService implements CanonicalProtocolExecutionService {

	private final ServiceExecutionEnviroment see;

	@Inject
	public PrototypeCanonicalProtocolExecutionService(ServiceExecutionEnviroment see) {
		super();
		this.see = see;
	}

	public CanonicalProtocolRequestData executeCanonicalCalls(ServiceExecutionRequest request) {
		dumpRequest(request);
		ExternalServiceReference reference = see.getExternalServiceReference(request.getTargetURL());
		CanonicalProtocolRequestData response = reference.handleRequest(request);
		dumpResponse(response);
		return response;
	}

	private void dumpResponse(CanonicalProtocolRequestData response) {

		System.out.println("CP: Canonical Protocol Execution. Response");
		System.out.println("CP: " + response.toString()) ;		
	}

	private void dumpRequest(ServiceExecutionRequest request) {
		System.out.println("CP: Canonical Protocol Execution. Request");
		System.out.println("CP: " + request.toString()) ;
		
	}


}
