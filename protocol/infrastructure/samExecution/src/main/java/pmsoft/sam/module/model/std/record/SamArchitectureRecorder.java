package pmsoft.sam.module.model.std.record;

import pmsoft.sam.definition.architecture.SamArchitectureLoader;
import pmsoft.sam.definition.architecture.SamCategoryLoader;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.definition.service.SamServiceLoader;
import pmsoft.sam.meta.TypeRegistry;
import pmsoft.sam.model.architecture.ServiceKey;

import com.google.inject.Inject;

public class SamArchitectureRecorder implements SamArchitectureLoader {

	private final TypeRegistry<String, SamCategoryLoader> categorySet;
	private final TypeRegistry<ServiceKey, SamServiceLoader> serviceSet;

	@Inject
	public SamArchitectureRecorder(TypeRegistry<String, SamCategoryLoader> categorySet,
			TypeRegistry<ServiceKey, SamServiceLoader> serviceSet) {
		super();
		this.categorySet = categorySet;
		this.serviceSet = serviceSet;
	}

	public SamCategoryLoader createCategory(String categoryName) {
		return categorySet.createInstance(categoryName);
	}

	public SamServiceLoader registerService(SamServiceDefinition serviceInstance) {
		Class<? extends SamServiceDefinition> serviceDefClass = serviceInstance.getClass();
		ServiceKey serviceKey = new ServiceKey(serviceDefClass.getCanonicalName());
		return serviceSet.createInstance(serviceKey);
	}

}
