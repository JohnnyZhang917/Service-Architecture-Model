package pmsoft.sam.module.model.std.record;

import pmsoft.sam.meta.TypeRegistry;
import pmsoft.sam.module.definition.architecture.SamServiceDefinition;
import pmsoft.sam.module.definition.architecture.grammar.SamArchitectureLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamCategoryLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamServiceLoader;
import pmsoft.sam.module.model.ServiceKey;

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
		ServiceKey serviceKey = new ServiceKey(serviceDefClass);
		return serviceSet.createInstance(serviceKey);
	}

}
