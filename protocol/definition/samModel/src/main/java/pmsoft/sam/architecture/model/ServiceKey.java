package pmsoft.sam.architecture.model;

public class ServiceKey {

	private final String serviceDefinitionSignature;

	public ServiceKey(String serviceDefinitionSignature) {
		assert serviceDefinitionSignature != null;
		this.serviceDefinitionSignature = serviceDefinitionSignature;
	}

	public String getServiceDefinitionSignature() {
		return serviceDefinitionSignature;
	}

	@Override
	public int hashCode() {
		return serviceDefinitionSignature.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceKey other = (ServiceKey) obj;
		if (serviceDefinitionSignature == null) {
			if (other.serviceDefinitionSignature != null)
				return false;
		} else if (!serviceDefinitionSignature
				.equals(other.serviceDefinitionSignature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServiceKey [" + serviceDefinitionSignature + "]";
	}

}
