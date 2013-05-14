package eu.pmsoft.see.api.data.impl.courier;

import com.google.inject.AbstractModule;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceContract;

public class TestCourierServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CourierServiceContract.class).to(TestCourierServiceContract.class).asEagerSingleton();
    }

}
