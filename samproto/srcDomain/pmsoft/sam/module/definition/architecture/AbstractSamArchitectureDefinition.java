package pmsoft.sam.module.definition.architecture;

import pmsoft.sam.module.definition.architecture.grammar.SamArchitectureLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamCategoryLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamServiceLoader;

public abstract class AbstractSamArchitectureDefinition implements
		SamArchitectureDefinition {

	private SamArchitectureLoader loaderRef;

	public abstract void loadArchitectureDefinition();

	protected SamCategoryLoader createCategory(String categoryName) {
		return loaderRef.createCategory(categoryName);
	}
	
	protected SamServiceLoader createService(
			AbstractSamServiceDefinition definition,SamCategoryLoader category){
		SamServiceLoader service = definition.loadServiceDefinition(loaderRef);
		category.addService(service);
		return service;
	}

	public final void loadArchitectureDefinition(SamArchitectureLoader loader) {
		try {
			this.loaderRef = loader;
			loadArchitectureDefinition();
		} finally {
			this.loaderRef = null;
		}
	}

}
