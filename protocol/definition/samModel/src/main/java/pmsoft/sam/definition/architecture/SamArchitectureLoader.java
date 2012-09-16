package pmsoft.sam.definition.architecture;



public interface SamArchitectureLoader {

	/**
	 * Category Class
	 * @param categoryName
	 * @return
	 */
	SamCategoryLoader createCategory(String categoryName);


}
