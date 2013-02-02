package pmsoft.sam.see.api.data.architecture;

import pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import pmsoft.sam.see.api.data.architecture.service.*;

public class SeeTestArchitecture extends AbstractSamArchitectureDefinition {

	@Override
	protected void loadArchitectureDefinition() {
		SamCategoryLoader test = createCategory("Test");
		test.withService(new TestServiceOne());
		test.withService(new TestServiceTwo());
		test.withService(new TestServiceZero());

		SamCategoryLoader shopping = createCategory("Shopping");
		shopping.withService(new ShoppingService());
		shopping.withService(new CourierService());
		shopping.withService(new StoreService());

	}

}
