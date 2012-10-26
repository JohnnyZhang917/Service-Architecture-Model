package pmsoft.sam.see.api.data.architecture;

import pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;

public class SeeTestArchitecture extends AbstractSamArchitectureDefinition {

	@Override
	protected void loadArchitectureDefinition() {
		SamCategoryLoader test = createCategory("Test");
		test.withService(new TestServiceOne());
		test.withService(new TestServiceTwo());
		test.withService(new TestServiceZero());
	}

}
