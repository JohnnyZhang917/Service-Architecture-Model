package pmsoft.sam.see.api.data.impl.courier;

import pmsoft.sam.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceContract;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceOrder;

public class TestCourierServiceContract implements CourierServiceContract {

	@Override
	public CourierServiceOrder openOrder(String contractId) {
		return new MyCourierServiceOrder();
	}

	class MyCourierServiceOrder implements CourierServiceOrder {
		String contractId;
		Integer price = 10000;
		
		@Override
		public CourierAddressSetupInfo setupAddress() {
			return new MyCourierAddressSetupInfo();
		}

		@Override
		public Integer getServicePrice() {
			return price;
		}

		class MyCourierAddressSetupInfo implements CourierAddressSetupInfo {

			@Override
			public void setCity(String cityName) {
				price = price - 100;
			}

			@Override
			public void setStreet(String streetName) {
				price = price - 100;
			}

			@Override
			public void setPackageSize(Integer kgSize) {
				price = price - 1000;
			}

			@Override
			public void setupDone() {
				
			}
			
		}
	}
	
}
