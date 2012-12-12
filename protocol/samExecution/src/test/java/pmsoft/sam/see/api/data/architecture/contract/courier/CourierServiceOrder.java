package pmsoft.sam.see.api.data.architecture.contract.courier;

public interface CourierServiceOrder {

	public CourierAddressSetupInfo setupAddress();
	
	public Integer getServicePrice();
}
