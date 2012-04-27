package pmsoft.sam.module.model;

import pmsoft.sam.module.definition.architecture.SamServiceDefinition;

public class ServiceKey {

	private final String serviceDefinitionSignature;

	public ServiceKey(Class<? extends SamServiceDefinition> serviceDefClass) {
		this.serviceDefinitionSignature = serviceDefClass.getCanonicalName();
	}
	
	public ServiceKey(String serviceDefinitionSignature) {
		super();
		this.serviceDefinitionSignature = serviceDefinitionSignature;
	}

	public String getServiceDefinitionSignature() {
		return serviceDefinitionSignature;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serviceDefinitionSignature == null) ? 0 : serviceDefinitionSignature.hashCode());
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
		} else if (!serviceDefinitionSignature.equals(other.serviceDefinitionSignature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServiceKey [" + serviceDefinitionSignature + "]";
	}

}
