package pmsoft.sam.protocol.execution;

import java.io.Serializable;
import java.net.URL;

public class CanonicalProtocolServiceEndpointLocation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6697694312658098127L;
	private final URL endpointLocation;

	public CanonicalProtocolServiceEndpointLocation(URL endpointLocation) {
		this.endpointLocation = endpointLocation;
	}

	public URL getEndpointLocation() {
		return endpointLocation;
	}

}
