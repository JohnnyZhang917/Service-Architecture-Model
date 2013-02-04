package eu.pmsoft.see.api.data.impl.store;

import com.google.inject.AbstractModule;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreServiceContract;

public class TestStoreServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(StoreServiceContract.class).to(TestStoreServiceContract.class).asEagerSingleton();
    }

}
