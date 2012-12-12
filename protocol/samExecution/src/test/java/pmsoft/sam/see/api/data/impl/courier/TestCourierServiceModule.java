package pmsoft.sam.see.api.data.impl.courier;

import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceContract;

import com.google.inject.AbstractModule;

public class TestCourierServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CourierServiceContract.class).to(TestCourierServiceContract.class).asEagerSingleton();
	}

}
