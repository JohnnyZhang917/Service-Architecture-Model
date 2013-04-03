package pmsoft.sam.see.api.data.impl.courier;

import com.google.inject.AbstractModule;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceContract;

public class TestCourierServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CourierServiceContract.class).to(TestCourierServiceContract.class).asEagerSingleton();
    }

}
