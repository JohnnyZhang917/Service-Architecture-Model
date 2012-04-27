package pmsoft.sam.module.definition.architecture.grammar;


public interface SamCategoryLoader{
	/**
	 * Relation Category -> Service
	 * @param serviceDefinition
	 */
	public void addService(SamServiceLoader serviceDefinition);

	/**
	 * Relation Category -> Category
	 * @param accesibleCategory
	 */
	public void addAccessToCategory(SamCategoryLoader accesibleCategory);

}
