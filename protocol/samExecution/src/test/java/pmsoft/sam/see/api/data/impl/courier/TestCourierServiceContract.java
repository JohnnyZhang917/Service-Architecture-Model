package pmsoft.sam.see.api.data.impl.courier;

import pmsoft.sam.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceContract;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceOrder;

import com.google.common.base.Preconditions;

public class TestCourierServiceContract implements CourierServiceContract {

	@Override
	public CourierServiceOrder openOrder(String contractId) {
		return new MyCourierServiceOrder();
	}

	class MyCourierServiceOrder implements CourierServiceOrder {
		String contractId;
		Integer price = 10000;
		MyCourierAddressSetupInfo info;
		
		@Override
		public CourierAddressSetupInfo setupAddress() {
			info =  new MyCourierAddressSetupInfo();
			return info;
		}

		@Override
		public Integer getServicePrice() {
			Preconditions.checkState(info.setupDone);
			return price;
		}

		class MyCourierAddressSetupInfo implements CourierAddressSetupInfo {

			boolean setupDone = false;
            int countcall = 0;
			@Override
			public void setCity(String cityName) {
				price = price - 100;
                countcall++;
			}

			@Override
			public void setStreet(String streetName) {
				price = price - 100;
                countcall++;
			}

			@Override
			public void setPackageSize(Integer kgSize) {
				price = price - 1000;
                countcall++;
			}

			@Override
			public boolean setupDone() {
                if( countcall > 2) {
                    setupDone = true;
                }
				return setupDone;
			}
			
		}
	}
	
}