package pmsoft.sam.module.definition.test.oauth.impl.faji;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

import pmsoft.sam.module.definition.test.oauth.service.PhotoResource;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoOrder;

public class PrintingPhotoOrderModel implements PrintingPhotoOrder {

	List<String> addressList = Lists.newArrayList();
	List<PhotoResource> photoList = Lists.newArrayList();

	public void addAdressInformation(String address) {
		addressList.add(address);
	}

	public List<String> registeredAddressList() {
		return addressList;
	}

	public void addPhotoResourceReference(PhotoResource photo) {
		photoList.add(photo);
	}

	public List<String> getPhotoIdList() {
		// This code creates one request for each photo to get the photoId. Some
		// more complex API specification can be created , but this example is
		// just to show the concept of OAuth provided by the Canonical protocol
		Builder<String> builder = ImmutableList.builder();
		for (PhotoResource photo : photoList) {
			builder.add(photo.getPhotoID());
		}
		return builder.build();
	}

	public boolean submitOrder() {
		if( photoList.size() == 0 || addressList.size() == 0) {
			System.out.println("The print order is not well filled, check service interaction");
			return false;
		}
		for (PhotoResource photo : photoList) {
			checkState(photo.getExtendedApi().checkNestedInterserviceInteraction());
			byte[] data = photo.getPhotoData();
			// in this example the data is actually a String
			String printedPhoto = new String(data);
			System.out.println("Printing photo");
			System.out.println(printedPhoto);
		}
		System.out.println("Sending photos to:");
		for (String sendTo : addressList) {
			System.out.println(sendTo);
		}
		System.out.println("Print submition done.");
		return true;
	}

}
