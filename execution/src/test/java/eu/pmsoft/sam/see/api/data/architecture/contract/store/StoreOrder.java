package eu.pmsoft.sam.see.api.data.architecture.contract.store;

import eu.pmsoft.sam.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;

public interface StoreOrder {

    public void addProduct(String productId, Integer nrOfProducts);

    public boolean receiveExternalCourierService(CourierAddressSetupInfo adressSetup);

    public boolean cancelOrder();

    public String realizeOrder();
}
