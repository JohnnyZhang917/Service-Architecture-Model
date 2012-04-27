package pmsoft.sam.module.definition.test.data;

import pmsoft.sam.module.definition.architecture.AbstractSamArchitectureDefinition;
import pmsoft.sam.module.definition.architecture.grammar.SamCategoryLoader;
import pmsoft.sam.module.definition.test.data.service.Service1Definition;
import pmsoft.sam.module.definition.test.data.service.Service2Definition;
import pmsoft.sam.module.definition.test.data.service.Service3Definition;
import pmsoft.sam.module.definition.test.data.service.Service4Definition;

public class TestArchitecture extends AbstractSamArchitectureDefinition {

	public void loadArchitectureDefinition() {
		SamCategoryLoader userCategory = createCategory("UserCategory");
		SamCategoryLoader dataCategory = createCategory("DataCategory");
		
		createService(new Service1Definition(), userCategory);
		createService(new Service2Definition(), dataCategory);
		createService(new Service3Definition(), dataCategory);
		createService(new Service4Definition(), dataCategory);
		
		userCategory.addAccessToCategory(dataCategory);
		dataCategory.addAccessToCategory(dataCategory);
	}

}
