package eu.pmsoft.sam.architecture.model;

import java.util.Set;

@Deprecated
public interface SamCategory {

    public Set<SamCategory> getAccessibleCategories();

    public Set<SamCategory> getInverseAccessibleCategories();

    public Set<SamService> getDefinedServices();
}
