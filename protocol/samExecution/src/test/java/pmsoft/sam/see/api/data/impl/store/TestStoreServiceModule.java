package pmsoft.sam.see.api.data.impl.store;

import pmsoft.sam.see.api.data.architecture.contract.store.StoreServiceContract;

import com.google.inject.AbstractModule;

public class TestStoreServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(StoreServiceContract.class).to(TestStoreServiceContract.class).asEagerSingleton();
	}

}
