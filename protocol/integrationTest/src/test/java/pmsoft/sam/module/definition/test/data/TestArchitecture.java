package pmsoft.sam.module.definition.test.data;

import pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import pmsoft.sam.module.definition.test.data.service.Service1Definition;
import pmsoft.sam.module.definition.test.data.service.Service2Definition;
import pmsoft.sam.module.definition.test.data.service.Service3Definition;
import pmsoft.sam.module.definition.test.data.service.Service4Definition;

public class TestArchitecture extends AbstractSamArchitectureDefinition {

	public void loadArchitectureDefinition() {
		SamCategoryLoader userCategory = createCategory("UserCategory");
		SamCategoryLoader dataCategory = createCategory("DataCategory");

		userCategory.withService(new Service1Definition());
		dataCategory.withService(new Service2Definition());
		dataCategory.withService(new Service3Definition());
		dataCategory.withService(new Service4Definition());
		
		userCategory.accessToCategory(dataCategory);
		dataCategory.accessToCategory(dataCategory);
	}

}
