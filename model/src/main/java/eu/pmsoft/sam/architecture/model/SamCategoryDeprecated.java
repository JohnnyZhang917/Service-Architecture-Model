package eu.pmsoft.sam.architecture.model;

import java.util.Set;

public interface SamCategoryDeprecated {

    public Set<SamCategoryDeprecated> getAccessibleCategories();

    public Set<SamCategoryDeprecated> getInverseAccessibleCategories();

    public Set<SamServiceDeprecated> getDefinedServices();
}
