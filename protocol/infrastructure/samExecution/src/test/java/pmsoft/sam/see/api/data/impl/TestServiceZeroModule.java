package pmsoft.sam.see.api.data.impl;

import pmsoft.sam.see.api.data.architecture.TestInterfaceZero;

import com.google.inject.AbstractModule;

public class TestServiceZeroModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TestInterfaceZero.class).toInstance(new TestInterfaceZero() {
			
			@Override
			public String ping(String value) {
				return "pong" + value;
			}
		});
	}

}
