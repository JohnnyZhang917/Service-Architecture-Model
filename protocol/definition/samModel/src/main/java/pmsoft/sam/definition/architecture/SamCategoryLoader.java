package pmsoft.sam.definition.architecture;

import pmsoft.sam.definition.service.SamServiceLoader;

public interface SamCategoryLoader extends SamServiceLoader {

	/**
	 * Relation Category -> Category
	 * 
	 * @param accesibleCategory
	 */
	public void addAccessToCategory(SamCategoryLoader accesibleCategory);

}
