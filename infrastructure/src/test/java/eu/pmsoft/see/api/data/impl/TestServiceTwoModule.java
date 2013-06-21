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
