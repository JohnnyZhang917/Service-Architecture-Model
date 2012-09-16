package pmsoft.sam.module.definition.test.oauth.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class PhotoSharingServiceDefinition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(PhotoSharingAPI.class);
	}

}
