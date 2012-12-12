package pmsoft.sam.see.api.data.impl.shopping;

import pmsoft.sam.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceContract;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceOrder;
import pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;
import pmsoft.sam.see.api.data.architecture.contract.store.StoreOrder;
import pmsoft.sam.see.api.data.architecture.contract.store.StoreServiceContract;

import com.google.inject.Inject;

public class TestShoppingStoreWithCourierInteraction implements ShoppingStoreWithCourierInteraction {

	private final CourierServiceContract courier;
	private final StoreServiceContract store;
	
	@Inject
	public TestShoppingStoreWithCourierInteraction(CourierServiceContract courier, StoreServiceContract store) {
		super();
		this.courier = courier;
		this.store = store;
	}


	@Override
	public Integer makeShoping() {
		StoreOrder order = store.createNewOrder();
		order.addProduct("a", 1);
		order.addProduct("b", 1);
		
		CourierServiceOrder contract = courier.openOrder("myContractId");
		CourierAddressSetupInfo address = contract.setupAddress();
        boolean addressSetupOk = order.receiveExternalCourierService(address);
        if( ! addressSetupOk ){
            throw  new RuntimeException("address setup interaction failed");
        }
        Integer price = contract.getServicePrice();
		System.err.println("order price is " + price);
		String orderDone = order.realizeOrder();
		System.err.println("My order contains:" + orderDone);
		return price;
	}

}
