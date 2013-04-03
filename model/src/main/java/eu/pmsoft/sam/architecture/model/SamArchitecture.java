package eu.pmsoft.sam.architecture.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface SamArchitecture {

    Collection<SamService> getAllService();

    Collection<SamCategory> getAllCategories();

    Collection<Annotation> getArchitectureAnnotations();

    SamCategory getCategory(String categoryId);

    SamService getService(ServiceKey serviceKey);

}
