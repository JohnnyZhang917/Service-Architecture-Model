package pmsoft.sam.module.definition.test.oauth.impl.faji;

import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoAPI;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoOrder;

public class FajiPrintingPhotoAPI implements PrintingPhotoAPI{

	public PrintingPhotoOrder createPrintingOrder() {
		return new PrintingPhotoOrderModel();
	}

}
