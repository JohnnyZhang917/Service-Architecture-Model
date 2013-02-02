package pmsoft.sam.see.api.data.impl;

import com.google.inject.AbstractModule;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceOne;
import pmsoft.sam.see.api.data.architecture.service.TestServiceOne;

public class TestServiceOneModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TestInterfaceOne.class).to(TestServiceOneImplementation.class);
        bind(TestInterfaceOne.class).annotatedWith(TestServiceOne.TEST_NAME).to(TestServiceOneImplementation.class);
	}
	
	public static class TestServiceOneImplementation implements TestInterfaceOne{
		@Override
		public boolean runTest() {
			return true;
		}
	}

}
