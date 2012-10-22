package pmsoft.sam.protocol.execution;

import java.util.UUID;

/**
 * 
 * @author Pawel Cesar Sanjuan Szklarz
 * 
 */
public class CanonicalProtocolRequest {

	/**
	 * forwardCall true for requests from client service to final implementation
	 * forwardCall false for requests generated execution of method calls to external instance references
	 */
	private final boolean forwardCall;
	private final UUID canonicalTransactionIdentificator;
	private final CanonicalProtocolServiceEndpointLocation target;
	private final CanonicalProtocolServiceEndpointLocation source;
	private final CanonicalProtocolRequestData data;
	private final int serviceSlotNr;
	
	public CanonicalProtocolRequest(boolean forwardCall, UUID canonicalTransactionIdentificator, CanonicalProtocolServiceEndpointLocation targetURL, CanonicalProtocolServiceEndpointLocation sourceURL,
			CanonicalProtocolRequestData data, int serviceSlotNr) {
		super();
		this.forwardCall = forwardCall;
		this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;
		this.target = targetURL;
		this.source = sourceURL;
		this.data = data;
		this.serviceSlotNr = serviceSlotNr;
	}

	public UUID getCanonicalTransactionIdentificator() {
		return canonicalTransactionIdentificator;
	}

	public CanonicalProtocolServiceEndpointLocation getTargetLocation() {
		return target;
	}

	public CanonicalProtocolRequestData getData() {
		return data;
	}

	public CanonicalProtocolServiceEndpointLocation getSourceLocation() {
		return source;
	}

	public boolean isForwardCall() {
		return forwardCall;
	}

	@Override
	public String toString() {
		return "ServiceExecutionRequest [forwardCall=" + forwardCall + ", canonicalTransactionIdentificator="
				+ canonicalTransactionIdentificator + ", target=" + target + ", source=" + source + ", data=" + data
				+ "]";
	}

	public int getServiceSlotNr() {
		return serviceSlotNr;
	}

}
