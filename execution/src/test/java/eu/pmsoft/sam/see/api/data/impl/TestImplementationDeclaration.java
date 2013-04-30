package eu.pmsoft.sam.see.api.data.impl;

import eu.pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import eu.pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition;
import eu.pmsoft.sam.see.api.data.architecture.service.*;
import eu.pmsoft.sam.see.api.data.impl.courier.TestCourierServiceModule;
import eu.pmsoft.sam.see.api.data.impl.shopping.TestShoppingModule;
import eu.pmsoft.sam.see.api.data.impl.store.TestStoreServiceModule;

public class TestImplementationDeclaration extends AbstractSamImplementationPackage {

    @Override
    public void packageDefinition() {

        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceZero>(TestServiceZero.class, TestServiceZeroModule.class) {
            @Override
            protected void implementationDefinition() {

            }
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceOne>(TestServiceOne.class, TestServiceOneModule.class) {
            @Override
            protected void implementationDefinition() {
            }
        });

        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceTwo>(TestServiceTwo.class, TestServiceTwoModule.class) {
            @Override
            protected void implementationDefinition() {
                withBindingsTo(TestServiceOne.class);
                withBindingsTo(TestServiceZero.class);
            }
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<StoreService>(StoreService.class, TestStoreServiceModule.class) {
            @Override
            protected void implementationDefinition() {
            }
        });

        registerImplementation(new AbstractSamServiceImplementationDefinition<CourierService>(CourierService.class, TestCourierServiceModule.class) {
            @Override
            protected void implementationDefinition() {
            }
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<ShoppingService>(ShoppingService.class, TestShoppingModule.class) {
            @Override
            protected void implementationDefinition() {
                withBindingsTo(StoreService.class);
                withBindingsTo(CourierService.class);

            }
        });
    }

}
