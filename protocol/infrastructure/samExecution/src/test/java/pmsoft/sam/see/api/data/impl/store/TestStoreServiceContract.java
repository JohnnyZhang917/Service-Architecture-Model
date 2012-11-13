package pmsoft.sam.see.api.data.impl.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pmsoft.sam.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import pmsoft.sam.see.api.data.architecture.contract.store.StoreOrder;
import pmsoft.sam.see.api.data.architecture.contract.store.StoreServiceContract;

public class TestStoreServiceContract implements StoreServiceContract {

	@Override
	public StoreOrder createNewOrder() {
		return new MyStoreOrder();
	}
	
	class MyStoreOrder implements StoreOrder {

		Map<String, Integer> order = new HashMap<String, Integer>();
		
		@Override
		public void addProduct(String productId, Integer nrOfProducts) {
			if(order.containsKey(productId)) {
				Integer current = order.get(productId);
				order.put(productId, current+nrOfProducts);
			} else {
				order.put(productId, nrOfProducts);
			}
		}

		@Override
		public void receiveExternalCourierService(CourierAddressSetupInfo adressSetup) {
			adressSetup.setCity("Warsaw");
			adressSetup.setPackageSize(order.size() + 1);
			adressSetup.setupDone();
		}

		@Override
		public boolean cancelOrder() {
			return false;
		}

		@Override
		public String realizeOrder() {
			StringBuffer buf = new StringBuffer();
			for (Entry<String, Integer> orderProduct : order.entrySet()) {
				buf.append("product:").append(orderProduct.getKey()).append("-").append(orderProduct.getValue()).append("\n");
			}
			return buf.toString();
		}
		
	}

}
