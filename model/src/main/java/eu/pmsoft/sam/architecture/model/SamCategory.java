package eu.pmsoft.sam.architecture.model;

import java.util.Set;

public interface SamCategory {

    public String getCategoryId();

    public Set<SamCategory> getAccessibleCategories();

    public Set<SamCategory> getInverseAccessibleCategories();

    public Set<SamService> getDefinedServices();
}
