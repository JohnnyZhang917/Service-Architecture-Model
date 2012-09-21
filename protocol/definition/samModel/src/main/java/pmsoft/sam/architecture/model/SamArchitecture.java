package pmsoft.sam.architecture.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Model of architecture.
 * @author pawel
 */
public interface SamArchitecture {

	/**
	 * Set of all defined service contact
	 * @return
	 */
	Collection<SamService> getAllService();
	
	/**
	 * Set of all categories
	 * @return
	 */
	Collection<SamCategory> getAllCategories();
	
	/**
	 * Set of allowed binding annotations for injection points related to external services.
	 * @return
	 */
	Collection<Annotation> getArchitectureAnnotations();
	
	/**
	 * Extract category by ID
	 * @param categoryId
	 * @return
	 */
	SamCategory getCategory(String categoryId);
	
	/**
	 * Extract service contract by Key
	 * @param serviceKey
	 * @return
	 */
	SamService getService(ServiceKey serviceKey);

}
