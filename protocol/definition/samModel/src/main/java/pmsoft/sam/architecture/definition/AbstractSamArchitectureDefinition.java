package pmsoft.sam.architecture.definition;

import pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;

/**
 * Definition of Architecture is contained in one subclass of AbstractSamArchitectureDefinition.
 * 
 * Definitions for categories and access relation is provided. ServiceDefinition are defined externally and linked with Class object.
 * 
 * In future for more complex architectures the definition mechanism may change. 
 * 
 * Architecture definition is not necessary to execute serviceInstance.
 * 
 * @author pawel
 *
 */
public abstract class AbstractSamArchitectureDefinition implements
		SamArchitectureDefinition {

	private SamArchitectureLoader loaderRef;

	protected abstract void loadArchitectureDefinition();

	protected SamCategoryLoader createCategory(String categoryName) {
		return loaderRef.createCategory(categoryName);
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
