package pmsoft.sam.definition.architecture;

import pmsoft.sam.definition.service.SamServiceDefinition;

public abstract class AbstractSamArchitectureDefinition implements
		SamArchitectureDefinition {

	private SamArchitectureLoader loaderRef;

	protected abstract void loadArchitectureDefinition();

	protected SamCategoryLoader createCategory(String categoryName) {
		return loaderRef.createCategory(categoryName);
	}

	protected void createServiceDefinition(
			SamServiceDefinition definition, SamCategoryLoader category) {
		definition.loadServiceDefinition(category);
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
