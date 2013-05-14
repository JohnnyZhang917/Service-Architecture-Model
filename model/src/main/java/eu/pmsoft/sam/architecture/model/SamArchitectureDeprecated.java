package eu.pmsoft.sam.architecture.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface SamArchitectureDeprecated {

    Collection<SamServiceDeprecated> getAllService();

    Collection<SamCategoryDeprecated> getAllCategories();

    Collection<Annotation> getArchitectureAnnotations();

    SamCategoryDeprecated getCategory(String categoryId);

    SamServiceDeprecated getService(ServiceKeyDeprecated serviceKeyDeprecated);

}
