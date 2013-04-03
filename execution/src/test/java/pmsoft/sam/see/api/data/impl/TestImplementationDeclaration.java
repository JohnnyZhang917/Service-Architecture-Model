package pmsoft.sam.see.api.data.impl;

import pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition;
import pmsoft.sam.see.api.data.architecture.service.*;
import pmsoft.sam.see.api.data.impl.courier.TestCourierServiceModule;
import pmsoft.sam.see.api.data.impl.shopping.TestShoppingModule;
import pmsoft.sam.see.api.data.impl.store.TestStoreServiceModule;

public class TestImplementationDeclaration extends AbstractSamImplementationPackage {

    @Override
    public void packageDefinition() {

        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceZero>(TestServiceZero.class) {
            @Override
            protected void implementationDefinition() {
                implementedInModule(TestServiceZeroModule.class);
            }
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceOne>(TestServiceOne.class) {
            @Override
            protected void implementationDefinition() {
                implementedInModule(TestServiceOneModule.class);
            }
        });

        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceTwo>(TestServiceTwo.class) {
            @Override
            protected void implementationDefinition() {
                withBindingsTo(TestServiceOne.class);
                withBindingsTo(TestServiceZero.class);
                implementedInModule(TestServiceTwoModule.class);
            }
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<StoreService>(StoreService.class) {
            @Override
            protected void implementationDefinition() {
                implementedInModule(TestStoreServiceModule.class);
            }
        });

        registerImplementation(new AbstractSamServiceImplementationDefinition<CourierService>(CourierService.class) {
            @Override
            protected void implementationDefinition() {
                implementedInModule(TestCourierServiceModule.class);
            }
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<ShoppingService>(ShoppingService.class) {
            @Override
            protected void implementationDefinition() {
                withBindingsTo(StoreService.class);
                withBindingsTo(CourierService.class);
                implementedInModule(TestShoppingModule.class);

            }
        });
    }

}
