package pmsoft.sam.see.api.data.impl;

import pmsoft.sam.see.api.data.architecture.TestInterfaceOne;
import pmsoft.sam.see.api.data.architecture.TestInterfaceTwo0;
import pmsoft.sam.see.api.data.architecture.TestInterfaceTwo1;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class TestServiceTwoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TestInterfaceTwo0.class).to(TestInterfaceTwo0Impl.class);
		bind(TestInterfaceTwo1.class).to(TestInterfaceTwo1Impl.class);
	}

	static public class TestInterfaceTwo0Impl implements TestInterfaceTwo0 {

		@Inject
		private TestInterfaceOne serviceOne;
		
		@Override
		public boolean runTest() {
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
