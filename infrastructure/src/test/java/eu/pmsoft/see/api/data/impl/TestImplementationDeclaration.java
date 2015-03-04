/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.see.api.data.impl;

import eu.pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import eu.pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition;
import eu.pmsoft.see.api.data.architecture.service.*;
import eu.pmsoft.see.api.data.impl.courier.TestCourierServiceModule;
import eu.pmsoft.see.api.data.impl.shopping.TestShoppingModule;
import eu.pmsoft.see.api.data.impl.store.TestStoreServiceModule;

public class TestImplementationDeclaration extends AbstractSamImplementationPackage {

    @Override
    public void packageDefinition() {

        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceZero>(TestServiceZero.class, TestServiceZeroModule.class) {
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceOne>(TestServiceOne.class, TestServiceOneModule.class) {
        });

        registerImplementation(new AbstractSamServiceImplementationDefinition<TestServiceTwo>(TestServiceTwo.class, TestServiceTwoModule.class) {
            @Override
            protected void implementationDefinition() {
                withBindingsTo(TestServiceOne.class);
                withBindingsTo(TestServiceZero.class);
            }
        });
        registerImplementation(new AbstractSamServiceImplementationDefinition<StoreService>(StoreService.class, TestStoreServiceModule.class) {
        });

        registerImplementation(new AbstractSamServiceImplementationDefinition<CourierService>(CourierService.class, TestCourierServiceModule.class) {
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
