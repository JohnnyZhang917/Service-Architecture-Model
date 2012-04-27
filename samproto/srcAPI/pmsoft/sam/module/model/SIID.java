package pmsoft.sam.module.model;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class SIID {

	private final UUID serviceInstanceID = UUID.randomUUID();

	private final ServiceKey serviceSpecification;

	private final ServiceImplementationKey implementationKey;

	private final ImmutableList<ServiceKey> injectionPoints;

	@Inject
	public SIID(ServiceKey serviceSpecification, ServiceImplementationKey implementationKey, List<ServiceKey> injectingServices) {
		super();
		this.serviceSpecification = serviceSpecification;
		this.implementationKey = implementationKey;
		this.injectionPoints = ImmutableList.copyOf(injectingServices);
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
