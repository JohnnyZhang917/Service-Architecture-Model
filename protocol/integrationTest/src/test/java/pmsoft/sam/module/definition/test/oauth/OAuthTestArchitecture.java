package pmsoft.sam.module.definition.test.oauth;

import pmsoft.sam.definition.architecture.AbstractSamArchitectureDefinition;
import pmsoft.sam.definition.architecture.SamCategoryLoader;
import pmsoft.sam.module.definition.test.oauth.service.JaneGuiServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoServiceDefinition;

public class OAuthTestArchitecture extends AbstractSamArchitectureDefinition {

	public void loadArchitectureDefinition() {
		SamCategoryLoader photoCategory = createCategory("PhotoCategory");
		SamCategoryLoader userCategory = createCategory("UserCategory");
		
		createServiceDefinition(new PhotoSharingServiceDefinition(), photoCategory);
		createServiceDefinition(new PrintingPhotoServiceDefinition(), photoCategory);
		createServiceDefinition(new JaneGuiServiceDefinition(), userCategory);

	}

}
