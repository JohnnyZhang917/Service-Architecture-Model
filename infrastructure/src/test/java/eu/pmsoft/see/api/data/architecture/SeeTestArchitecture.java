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

package eu.pmsoft.see.api.data.architecture;

import eu.pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import eu.pmsoft.see.api.data.architecture.service.*;

public class SeeTestArchitecture extends AbstractSamArchitectureDefinition {

    public static String signature = "Test Architecture 0.1";

    public SeeTestArchitecture() {
        super(signature);
    }

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
