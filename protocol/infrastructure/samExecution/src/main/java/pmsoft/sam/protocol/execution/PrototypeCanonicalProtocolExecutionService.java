package pmsoft.sam.protocol.execution;

import java.util.UUID;

import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionContext;
import pmsoft.sam.protocol.injection.internal.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.injection.internal.ServiceExecutionRequest;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.model.SIURL;

import com.google.inject.Inject;

class PrototypeCanonicalProtocolExecutionService implements CanonicalProtocolExecutionService {

	private final SamExecutionNode localExecutionNode;
	
	@Inject
	public PrototypeCanonicalProtocolExecutionService(SamExecutionNode localExecutionNode) {
		this.localExecutionNode = localExecutionNode;
	}

	@Override
	public CanonicalProtocolRequestData executeCanonicalCalls(ServiceExecutionRequest request) {
		System.out.println("Execution Request:\n" + request);
		SIURL targetUrl = request.getTargetURL();
		UUID transactionUniqueId = request.getCanonicalTransactionIdentificator();
		CanonicalProtocolExecutionContext remoteContext = localExecutionNode.openTransactionExecutionContext(targetUrl,transactionUniqueId);
		CanonicalProtocolRequestData responce = remoteContext.handleRequest(request);
		System.out.println("Responce is:\n" + responce);
		return responce;
		
	}

}
