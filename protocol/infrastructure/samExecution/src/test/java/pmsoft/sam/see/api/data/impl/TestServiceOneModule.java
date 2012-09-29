package pmsoft.sam.see.api.data.impl;

import pmsoft.sam.see.api.data.architecture.TestInterfaceOne;

import com.google.inject.AbstractModule;

public class TestServiceOneModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TestInterfaceOne.class).to(TestServiceOneImplementation.class);
	}
	
	public static class TestServiceOneImplementation implements TestInterfaceOne{

		@Override
		public boolean runTest() {
			return true;
		}

	}

}
