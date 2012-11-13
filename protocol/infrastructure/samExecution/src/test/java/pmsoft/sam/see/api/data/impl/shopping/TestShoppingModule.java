package pmsoft.sam.see.api.data.impl.shopping;

import pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;

import com.google.inject.AbstractModule;

public class TestShoppingModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ShoppingStoreWithCourierInteraction.class).to(TestShoppingStoreWithCourierInteraction.class);
	}

}
