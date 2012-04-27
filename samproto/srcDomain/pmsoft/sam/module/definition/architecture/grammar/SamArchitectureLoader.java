package pmsoft.sam.module.definition.architecture.grammar;

import pmsoft.sam.module.definition.architecture.SamServiceDefinition;

public interface SamArchitectureLoader {

	/**
	 * Category Class
	 * @param categoryName
	 * @return
	 */
	SamCategoryLoader createCategory(String categoryName);

	/**
	 * Service class
	 * 
	 * @param serviceInstance
	 * @return
	 */
	SamServiceLoader registerService(SamServiceDefinition serviceInstance);

}
