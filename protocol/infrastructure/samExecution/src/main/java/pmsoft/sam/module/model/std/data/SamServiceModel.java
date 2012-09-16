package pmsoft.sam.module.model.std.data;

import java.util.Set;

import pmsoft.sam.model.architecture.SamArchitecture;
import pmsoft.sam.model.architecture.SamService;
import pmsoft.sam.model.architecture.ServiceKey;

public class SamServiceModel implements SamService {

	private final ServiceKey serviceKey;
	private final SamArchitecture model;

	public SamServiceModel(ServiceKey serviceKey, SamArchitecture model) {
		super();
		this.serviceKey = serviceKey;
		this.model = model;
	}

	public ServiceKey getServiceKey() {
		return serviceKey;
	}

	public Set<Class<?>> getServiceInterfaces() {
		return model.getServiceInterfaces(serviceKey);
	}

}
