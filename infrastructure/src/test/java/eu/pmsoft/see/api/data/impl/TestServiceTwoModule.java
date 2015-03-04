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
import com.google.inject.Inject;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceOne;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceTwo0;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceTwo1;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceZero;
import org.testng.Assert;

public class TestServiceTwoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TestInterfaceTwo0.class).to(TestInterfaceTwo0Impl.class);
        bind(TestInterfaceTwo1.class).to(TestInterfaceTwo1Impl.class);
    }

    static public class TestInterfaceTwo0Impl implements TestInterfaceTwo0 {

        @Inject
        private TestInterfaceOne serviceOne;

        @Inject
        private TestInterfaceZero serviceZero;

        @Override
        public boolean runTest() {
            String pingReturn = serviceZero.ping("ping");
            Assert.assertNotNull(pingReturn);
            pingReturn = serviceZero.ping("ping2");
            Assert.assertNotNull(pingReturn);
            return serviceOne.runTest();
        }


    }

    static public class TestInterfaceTwo1Impl implements TestInterfaceTwo1 {
        @Inject
        private TestInterfaceOne serviceOne;

        @Override
        public void runDeep(int deep) {

        }

        @Override
        public boolean runTest() {
            return serviceOne.runTest();
        }

    }

}
