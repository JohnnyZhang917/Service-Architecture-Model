package pmsoft.sam.see.api.data.impl;

import pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import pmsoft.sam.see.api.data.architecture.service.CourierService;
import pmsoft.sam.see.api.data.architecture.service.ShoppingService;
import pmsoft.sam.see.api.data.architecture.service.StoreService;
import pmsoft.sam.see.api.data.architecture.service.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.service.TestServiceTwo;
import pmsoft.sam.see.api.data.architecture.service.TestServiceZero;
import pmsoft.sam.see.api.data.impl.courier.TestCourierServiceModule;
import pmsoft.sam.see.api.data.impl.shopping.TestShoppingModule;
import pmsoft.sam.see.api.data.impl.store.TestStoreServiceModule;

public class TestImplementationDeclaration extends AbstractSamImplementationPackage {

	@Override
	public void implementationDefinition() {
		provideContract(TestServiceZero.class).implementedInModule(TestServiceZeroModule.class);

		provideContract(TestServiceOne.class).implementedInModule(TestServiceOneModule.class);

		provideContract(TestServiceTwo.class).withBindingsTo(TestServiceOne.class).withBindingsTo(TestServiceZero.class)
				.implementedInModule(TestServiceTwoModule.class);
		
		
		provideContract(StoreService.class).implementedInModule(TestStoreServiceModule.class);
		
		provideContract(CourierService.class).implementedInModule(TestCourierServiceModule.class);
		
		provideContract(ShoppingService.class)
			.withBindingsTo(StoreService.class)
			.withBindingsTo(CourierService.class)
			.implementedInModule(TestShoppingModule.class);

	}

}
