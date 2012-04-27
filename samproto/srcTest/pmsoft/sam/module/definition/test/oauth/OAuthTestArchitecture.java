package pmsoft.sam.module.definition.test.oauth;

import pmsoft.sam.module.definition.architecture.AbstractSamArchitectureDefinition;
import pmsoft.sam.module.definition.architecture.grammar.SamCategoryLoader;
import pmsoft.sam.module.definition.test.oauth.service.JaneGuiServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoServiceDefinition;

public class OAuthTestArchitecture extends AbstractSamArchitectureDefinition {

	public void loadArchitectureDefinition() {
		SamCategoryLoader photoCategory = createCategory("PhotoCategory");
		SamCategoryLoader userCategory = createCategory("UserCategory");

		createService(new PhotoSharingServiceDefinition(), photoCategory);
		createService(new PrintingPhotoServiceDefinition(), photoCategory);
		createService(new JaneGuiServiceDefinition(), userCategory);

	}

}
