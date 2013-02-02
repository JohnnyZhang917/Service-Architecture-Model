package pmsoft.sam.see.api.data.impl.shopping;

import com.google.inject.AbstractModule;
import pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;

public class TestShoppingModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ShoppingStoreWithCourierInteraction.class).to(TestShoppingStoreWithCourierInteraction.class);
	}

}
