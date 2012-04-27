package pmsoft.sam.module.definition.test.oauth.impl;

import pmsoft.sam.module.definition.implementation.AbstractSamServiceImplementationDefinition;
import pmsoft.sam.module.definition.test.oauth.impl.beppa.BeppaPrintingPhotoService;
import pmsoft.sam.module.definition.test.oauth.impl.faji.FajiPhotoSharingService;
import pmsoft.sam.module.definition.test.oauth.impl.jane.JaneGuiServiceModule;
import pmsoft.sam.module.definition.test.oauth.service.JaneGuiServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoServiceDefinition;

public class OAuthServicesImplementationPackage extends AbstractSamServiceImplementationDefinition {

	@Override
	public void implementationDefinition() {
		registerImplementationOf(PrintingPhotoServiceDefinition.class).givenByModule(FajiPhotoSharingService.class).done();
		registerImplementationOf(PhotoSharingServiceDefinition.class).givenByModule(BeppaPrintingPhotoService.class).done();

		registerImplementationOf(JaneGuiServiceDefinition.class).givenByModule(JaneGuiServiceModule.class)
				.accessTo(PrintingPhotoServiceDefinition.class).accessTo(PhotoSharingServiceDefinition.class).done();

	}
}
