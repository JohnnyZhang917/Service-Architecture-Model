package pmsoft.sam.model.instance;

import java.util.List;
import java.util.UUID;

import pmsoft.sam.architecture.model.ServiceKey;

public class SIID {

	private final UUID serviceInstanceID = UUID.randomUUID();

	private final ServiceKey serviceSpecification;

	private final ServiceImplementationKey implementationKey;

	private final List<ServiceKey> injectionPoints;

	public SIID(ServiceKey serviceSpecification, ServiceImplementationKey implementationKey, List<ServiceKey> injectingServices) {
		super();
		this.serviceSpecification = serviceSpecification;
		this.implementationKey = implementationKey;
		this.injectionPoints = injectingServices;
	}

	public UUID getServiceInstanceID() {
		return serviceInstanceID;
	}

	@Override
	public int hashCode() {
		return serviceInstanceID.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SIID other = (SIID) obj;
		if (!serviceInstanceID.equals(other.serviceInstanceID)) {
			return false;
		}
		return true;
	}

	public ServiceKey getServiceSpecification() {
		return serviceSpecification;
	}

	public ServiceImplementationKey getImplementationKey() {
		return implementationKey;
	}

	public List<ServiceKey> getInjectionPoints() {
		return injectionPoints;
	}

	@Override
	public String toString() {
		return "SIID [serviceInstanceID=" + serviceInstanceID + ", implementationKey=" + implementationKey + "]";
	}

}
