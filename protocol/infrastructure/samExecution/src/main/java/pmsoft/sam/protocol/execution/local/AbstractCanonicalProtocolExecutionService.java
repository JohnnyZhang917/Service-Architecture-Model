package pmsoft.sam.protocol.execution.local;

@Deprecated
public abstract class AbstractCanonicalProtocolExecutionService { 
//implements CanonicalProtocolExecutionServiceClientApi {
//
//	private final SamExecutionNode localExecutionNode;
//	
//	@Inject
//	public AbstractCanonicalProtocolExecutionService(SamExecutionNode localExecutionNode) {
//		this.localExecutionNode = localExecutionNode;
//	}
//
//	public CanonicalProtocolRequestData internalExecuteCanonicalCalls(CanonicalProtocolRequest request) {
//		CanonicalProtocolServiceEndpointLocation target = request.getTargetLocation();
//		UUID transactionUniqueId = request.getCanonicalTransactionIdentificator();
//		SIURL targetUrl = new SIURL(target.getEndpointLocation().toString());
//		CanonicalProtocolExecutionContext remoteContext = localExecutionNode.openTransactionExecutionContext(targetUrl ,transactionUniqueId,request.isForwardCall());
//		CanonicalProtocolRequestData responce = remoteContext.handleRequest(request);
//		return responce;
//	}
}
