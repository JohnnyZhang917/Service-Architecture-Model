package pmsoft.sam.see.api.data.impl.store;

import com.google.inject.AbstractModule;
import pmsoft.sam.see.api.data.architecture.contract.store.StoreServiceContract;

public class TestStoreServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(StoreServiceContract.class).to(TestStoreServiceContract.class).asEagerSingleton();
	}

}
