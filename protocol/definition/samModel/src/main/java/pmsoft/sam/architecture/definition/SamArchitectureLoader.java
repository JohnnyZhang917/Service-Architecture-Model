package pmsoft.sam.architecture.definition;

import pmsoft.sam.definition.service.SamServiceDefinition;


/**
 * Regular grammar for loading architecture information:
 * 
 * Architecture = Category*
 * Category     = ServiceDefinition* CategoryAccess*
 * @author pawel
 *
 */
public interface SamArchitectureLoader {

	/**
	 * Create a new category 
	 * @param categoryName
	 * @return
	 */
	SamCategoryLoader createCategory(String categoryName);

	static public interface SamCategoryLoader {

		/**
		 * Relation Category - access -> Category
		 * 
		 * @param accesibleCategory
		 */
		public SamCategoryLoader accessToCategory(SamCategoryLoader accesibleCategory);
		
		/**
		 * Relation Category - contains -> ServiceDefinition
		 * @param serviceDefinitionClass class with serviceDefinition
		 * @return
		 */
		public SamCategoryLoader withService(SamServiceDefinition serviceDefinition);

	}

}
