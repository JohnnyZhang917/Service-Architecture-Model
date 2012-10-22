package pmsoft.sam.protocol.execution.local;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceProviderApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;

import com.google.inject.Inject;

class PrototypeCanonicalProtocolExecutionServiceClientApi implements CanonicalProtocolExecutionServiceClientApi {

	public final CanonicalProtocolExecutionServiceProviderApi localProvider;
	
	@Inject
	public PrototypeCanonicalProtocolExecutionServiceClientApi(CanonicalProtocolExecutionServiceProviderApi localProvider) {
		this.localProvider = localProvider;
	}


	public CanonicalProtocolRequestData executeExternalCanonicalRequest(CanonicalProtocolRequest request) {
		return localProvider.handleCanonicalRequest(request);
	}




}
