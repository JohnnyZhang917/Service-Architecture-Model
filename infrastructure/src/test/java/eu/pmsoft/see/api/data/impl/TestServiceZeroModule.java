package eu.pmsoft.see.api.data.impl;

import com.google.inject.AbstractModule;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceZero;

public class TestServiceZeroModule extends AbstractModule {

    public static class TestServiceZeroInternalImplementation implements TestInterfaceZero {
        @Override
        public String ping(String value) {
            return "pong" + value;
        }
    }


    @Override
    protected void configure() {
        bind(TestInterfaceZero.class).to(TestServiceZeroInternalImplementation.class);
    }

}
