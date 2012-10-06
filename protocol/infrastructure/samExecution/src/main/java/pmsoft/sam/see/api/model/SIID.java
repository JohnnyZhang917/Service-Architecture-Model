package pmsoft.sam.see.api.model;

import java.util.UUID;

public class SIID implements ServiceInstanceReference {

	private final UUID serviceInstanceID = UUID.randomUUID();

	public UUID getServiceInstanceID() {
		return serviceInstanceID;
	}

	@Override
	public String toString() {
		return "SIID [" + serviceInstanceID + "]";
	}

}
