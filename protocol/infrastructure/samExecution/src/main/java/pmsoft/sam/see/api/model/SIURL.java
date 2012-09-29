package pmsoft.sam.see.api.model;

import java.net.URL;

public class SIURL {

	private final URL serviceInstanceReference;

	public SIURL(URL serviceInstanceReference) {
		this.serviceInstanceReference = serviceInstanceReference;
	}

	public URL getServiceInstanceReference() {
		return serviceInstanceReference;
	}

	@Override
	public String toString() {
		return "SIURL [" + serviceInstanceReference + "]";
	}
	
}
