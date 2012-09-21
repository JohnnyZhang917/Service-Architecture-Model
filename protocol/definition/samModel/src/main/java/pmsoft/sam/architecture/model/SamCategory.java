package pmsoft.sam.architecture.model;

import java.util.Set;

public interface SamCategory {

	/**
	 * Unique category ID
	 * @return
	 */
	public String getCategoryId();

	/**
	 * Return the set of accessible categories from service in this category
	 * 
	 * @return
	 */
	public Set<SamCategory> getAccessibleCategories();

	/**
	 * Return the set of categories that have access to this category
	 * 
	 * @return
	 */
	public Set<SamCategory> getInverseAccessibleCategories();
	
	/**
	 * The set of defined services belonging to this Category
	 * @return
	 */
	public Set<SamService> getDefinedServices();
}
