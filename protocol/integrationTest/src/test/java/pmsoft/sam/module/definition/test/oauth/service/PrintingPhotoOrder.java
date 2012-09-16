package pmsoft.sam.module.definition.test.oauth.service;

import java.util.List;

public interface PrintingPhotoOrder {

	public void addAdressInformation(String address);

	public List<String> registeredAddressList();

	public void addPhotoResourceReference(PhotoResource photo);

	public List<String> getPhotoIdList();

	public boolean submitOrder();
}
