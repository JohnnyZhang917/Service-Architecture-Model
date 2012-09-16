package pmsoft.sam.module.definition.test.data;

import pmsoft.sam.definition.architecture.AbstractSamArchitectureDefinition;
import pmsoft.sam.definition.architecture.SamCategoryLoader;
import pmsoft.sam.module.definition.test.data.service.Service1Definition;
import pmsoft.sam.module.definition.test.data.service.Service2Definition;
import pmsoft.sam.module.definition.test.data.service.Service3Definition;
import pmsoft.sam.module.definition.test.data.service.Service4Definition;

public class TestArchitecture extends AbstractSamArchitectureDefinition {

	public void loadArchitectureDefinition() {
		SamCategoryLoader userCategory = createCategory("UserCategory");
		SamCategoryLoader dataCategory = createCategory("DataCategory");
		
		createServiceDefinition(new Service1Definition(), userCategory);
		createServiceDefinition(new Service2Definition(), dataCategory);
		createServiceDefinition(new Service3Definition(), dataCategory);
		createServiceDefinition(new Service4Definition(), dataCategory);
		
		userCategory.addAccessToCategory(dataCategory);
		dataCategory.addAccessToCategory(dataCategory);
	}

}
