package pmsoft.sam.protocol.execution.local;

import java.util.UUID;

import com.google.inject.Inject;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.execution.CanonicalProtocolServiceEndpointLocation;
import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionContext;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.model.SIURL;

public abstract class AbstractCanonicalProtocolExecutionService implements CanonicalProtocolExecutionServiceClientApi {

	private final SamExecutionNode localExecutionNode;
	
	@Inject
	public AbstractCanonicalProtocolExecutionService(SamExecutionNode localExecutionNode) {
		this.localExecutionNode = localExecutionNode;
	}

	public CanonicalProtocolRequestData internalExecuteCanonicalCalls(CanonicalProtocolRequest request) {
		System.out.println("Execution Request:\n" + request);
		CanonicalProtocolServiceEndpointLocation target = request.getTargetLocation();
		UUID transactionUniqueId = request.getCanonicalTransactionIdentificator();
		SIURL targetUrl = new SIURL(target.getEndpointLocation().toString());
		CanonicalProtocolExecutionContext remoteContext = localExecutionNode.openTransactionExecutionContext(targetUrl ,transactionUniqueId);
		CanonicalProtocolRequestData responce = remoteContext.handleRequest(request);
		System.out.println("Responce is:\n" + responce);
		return responce;
	}
}
