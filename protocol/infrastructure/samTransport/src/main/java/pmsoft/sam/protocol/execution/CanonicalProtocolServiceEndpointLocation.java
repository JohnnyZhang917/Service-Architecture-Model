package pmsoft.sam.protocol.execution;

import java.net.URL;

public class CanonicalProtocolServiceEndpointLocation {

	private final URL endpointLocation;

	public CanonicalProtocolServiceEndpointLocation(URL endpointLocation) {
		this.endpointLocation = endpointLocation;
	}

	public URL getEndpointLocation() {
		return endpointLocation;
	}

}
