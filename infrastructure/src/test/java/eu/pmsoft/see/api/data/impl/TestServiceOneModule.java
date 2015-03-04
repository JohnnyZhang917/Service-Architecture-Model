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

import com.google.inject.AbstractModule;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceOne;
import eu.pmsoft.see.api.data.architecture.service.TestServiceOne;

public class TestServiceOneModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TestInterfaceOne.class).to(TestServiceOneImplementation.class);
        bind(TestInterfaceOne.class).annotatedWith(TestServiceOne.TEST_NAME).to(TestServiceOneImplementation.class);
    }

    public static class TestServiceOneImplementation implements TestInterfaceOne {
        @Override
        public boolean runTest() {
            return true;
        }
    }

}
