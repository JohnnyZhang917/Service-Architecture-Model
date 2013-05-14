package eu.pmsoft.see.api.data.impl.shopping;

import com.google.inject.Inject;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceContract;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceOrder;
import eu.pmsoft.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreOrder;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreServiceContract;

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
        if (!addressSetupOk) {
            throw new RuntimeException("address setup interaction failed");
        }
        Integer price = contract.getServicePrice();
        System.err.println("order price is " + price);
        String orderDone = order.realizeOrder();
        System.err.println("My order contains:" + orderDone);
        return price;
    }

}
