package pmsoft.sam.protocol.injection.internal;

import java.util.UUID;

import pmsoft.sam.see.api.model.SIURL;

/**
 * For prototype implementation java serialization is used. Target
 * implementation to custom protocol definition
 * 
 * @author Pawel Cesar Sanjuan Szklarz
 * 
 */
public class ServiceExecutionRequest {

	/**
	 * forwardCall true for requests from client service to final implementation
	 * forwardCall false for requests generated execution of method calls to external instance references
	 */
	private final boolean forwardCall;
	private final UUID canonicalTransactionIdentificator;
	private final SIURL targetURL;
	private final SIURL sourceURL;
	private final CanonicalProtocolRequestData data;
	private final int serviceSlotNr;
	
	public ServiceExecutionRequest(boolean forwardCall, UUID canonicalTransactionIdentificator, SIURL targetURL, SIURL sourceURL,
			CanonicalProtocolRequestData data, int serviceSlotNr) {
		super();
		this.forwardCall = forwardCall;
		this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;
		this.targetURL = targetURL;
		this.sourceURL = sourceURL;
		this.data = data;
		this.serviceSlotNr = serviceSlotNr;
	}

	public UUID getCanonicalTransactionIdentificator() {
		return canonicalTransactionIdentificator;
	}

	public SIURL getTargetURL() {
		return targetURL;
	}

	public CanonicalProtocolRequestData getData() {
		return data;
	}

	public SIURL getSourceURL() {
		return sourceURL;
	}

	public boolean isForwardCall() {
		return forwardCall;
	}

	@Override
	public String toString() {
		return "ServiceExecutionRequest [forwardCall=" + forwardCall + ", canonicalTransactionIdentificator="
				+ canonicalTransactionIdentificator + ", targetURL=" + targetURL + ", sourceURL=" + sourceURL + ", data=" + data
				+ "]";
	}

	public int getServiceSlotNr() {
		return serviceSlotNr;
	}

}
