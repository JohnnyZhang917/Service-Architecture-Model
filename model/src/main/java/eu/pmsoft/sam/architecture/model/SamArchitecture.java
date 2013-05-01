package eu.pmsoft.sam.architecture.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

@Deprecated
public interface SamArchitecture {

    Collection<SamServiceDeprecated> getAllService();

    Collection<SamCategory> getAllCategories();

    Collection<Annotation> getArchitectureAnnotations();

    SamCategory getCategory(String categoryId);

    SamServiceDeprecated getService(ServiceKey serviceKey);

}
