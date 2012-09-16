package pmsoft.sam.module.model.std.data;

import java.util.Set;

import pmsoft.sam.model.architecture.SamArchitecture;
import pmsoft.sam.model.architecture.SamCategory;
import pmsoft.sam.model.architecture.SamService;

public class SamCategoryModel implements SamCategory {

	private final String categoryId;
	private final SamArchitecture model;
	
	public SamCategoryModel(String categoryId, SamArchitectureModel model) {
		super();
		this.categoryId = categoryId;
		this.model = model;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public Set<SamCategory> getAccessibleCategories() {
		return model.getAccessibleCategories(categoryId);
	}

	public Set<SamCategory> getInverseAccessibleCategories() {
		return model.getInverseAccessibleCategories(categoryId);
	}

	public Set<SamService> getDefinedServices() {
		return model.getServiceInCategory(categoryId);
	}

}
