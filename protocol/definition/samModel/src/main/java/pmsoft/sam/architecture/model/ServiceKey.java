package pmsoft.sam.architecture.model;

import pmsoft.sam.definition.service.SamServiceDefinition;

public class ServiceKey {

	private final String serviceDefinitionSignature;

	public ServiceKey(Class<? extends SamServiceDefinition> definitionClass) {
		assert definitionClass != null;
		this.serviceDefinitionSignature = definitionClass.getCanonicalName();
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
