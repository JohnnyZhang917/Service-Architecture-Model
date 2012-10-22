package pmsoft.sam.see.api.model;

import java.net.MalformedURLException;
import java.net.URL;

public class SIURL {

	private final URL serviceInstanceReference;

	public SIURL( URL serviceLocation) {
		this.serviceInstanceReference = serviceLocation;
	}
	
	public SIURL(String serviceInstanceReference) {
		try {
			this.serviceInstanceReference = new URL(serviceInstanceReference);
		} catch (MalformedURLException e) {
			//TODO exception policy
			throw new RuntimeException(serviceInstanceReference);
		}
	}

	public URL getServiceInstanceReference() {
		return serviceInstanceReference;
	}

	@Override
	public String toString() {
		return "SIURL [" + serviceInstanceReference + "]";
	}

	@Override
	public int hashCode() {
		return serviceInstanceReference.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SIURL other = (SIURL) obj;
		if (serviceInstanceReference == null) {
			if (other.serviceInstanceReference != null)
				return false;
		} else if (!serviceInstanceReference.equals(other.serviceInstanceReference))
			return false;
		return true;
	}

}