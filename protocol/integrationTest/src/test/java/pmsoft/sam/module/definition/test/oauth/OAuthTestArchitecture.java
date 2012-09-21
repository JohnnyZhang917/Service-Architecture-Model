package pmsoft.sam.module.definition.test.oauth;

import pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import pmsoft.sam.module.definition.test.oauth.service.JaneGuiServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoServiceDefinition;

public class OAuthTestArchitecture extends AbstractSamArchitectureDefinition {

	public void loadArchitectureDefinition() {
		SamCategoryLoader photoCategory = createCategory("PhotoCategory");
		SamCategoryLoader userCategory = createCategory("UserCategory");

		photoCategory.withService(new PhotoSharingServiceDefinition());
		photoCategory.withService(new PrintingPhotoServiceDefinition());
		userCategory.withService(new JaneGuiServiceDefinition());
		
	}

}
