package pmsoft.sam.canonical.deprecated.model;

import java.net.URL;
import java.util.UUID;

/**
 * For prototype implementation java serialization is used. Target
 * implementation to custom protocol definition
 * 
 * @author Paweł Cesar Sanjuan Szklarz
 * 
 */
public class ServiceExecutionRequest {

	/**
	 * forwardCall true for requests from client service to final implementation
	 * forwardCall false for requests generated execution of method calls to external instance references
	 */
	private final boolean forwardCall;
	private final UUID canonicalTransactionIdentificator;
	private final URL targetURL;
	private final URL sourceURL;
	private final CanonicalProtocolRequestData data;
	private final int serviceSlotNr;
	
	public ServiceExecutionRequest(boolean forwardCall, UUID canonicalTransactionIdentificator, URL targetURL, URL sourceURL,
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

	public URL getTargetURL() {
		return targetURL;
	}

	public CanonicalProtocolRequestData getData() {
		return data;
	}

	public URL getSourceURL() {
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
