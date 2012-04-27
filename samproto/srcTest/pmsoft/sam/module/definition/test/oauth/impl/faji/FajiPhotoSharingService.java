package pmsoft.sam.module.definition.test.oauth.impl.faji;

import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoAPI;

import com.google.inject.AbstractModule;

public class FajiPhotoSharingService extends AbstractModule {

	@Override
	protected void configure() {
		bind(PrintingPhotoAPI.class).to(FajiPrintingPhotoAPI.class).asEagerSingleton();
	}

}
