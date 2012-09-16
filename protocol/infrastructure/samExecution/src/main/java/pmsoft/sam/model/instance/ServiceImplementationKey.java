package pmsoft.sam.model.instance;

import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.model.architecture.ServiceKey;

import com.google.inject.Module;

/**
 * @author Paweł Cesar Sanjuan Szklarz
 * 
 */
public class ServiceImplementationKey {
	private final String serviceSpecificationSignature;
	private final String serviceImplementationSignature;

	public ServiceImplementationKey(
			Class<? extends SamServiceDefinition> serviceSpecificationSignature,
			Class<? extends Module> serviceImplementationSignature) {
		super();
		this.serviceSpecificationSignature = serviceSpecificationSignature
				.getCanonicalName();
		this.serviceImplementationSignature = serviceImplementationSignature
				.getCanonicalName();
	}

	public ServiceImplementationKey(String serviceSpecificationSignature,
			String serviceImplementationSignature) {
		super();
		this.serviceSpecificationSignature = serviceSpecificationSignature;
		this.serviceImplementationSignature = serviceImplementationSignature;
	}

	public ServiceKey getServiceSpecificationKey() {
		return new ServiceKey(serviceSpecificationSignature);
	}

	public String getServiceSpecificationSignature() {
		return serviceSpecificationSignature;
	}

	public String getServiceImplementationSignature() {
		return serviceImplementationSignature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((serviceImplementationSignature == null) ? 0
						: serviceImplementationSignature.hashCode());
		result = prime
				* result
				+ ((serviceSpecificationSignature == null) ? 0
						: serviceSpecificationSignature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceImplementationKey other = (ServiceImplementationKey) obj;
		if (serviceImplementationSignature == null) {
			if (other.serviceImplementationSignature != null)
				return false;
		} else if (!serviceImplementationSignature
				.equals(other.serviceImplementationSignature))
			return false;
		if (serviceSpecificationSignature == null) {
			if (other.serviceSpecificationSignature != null)
				return false;
		} else if (!serviceSpecificationSignature
				.equals(other.serviceSpecificationSignature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServiceImplementationKey [s=" + serviceSpecificationSignature
				+ ", i=" + serviceImplementationSignature + "]";
	}

}
