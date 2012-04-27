package pmsoft.sam.module.definition.test.oauth.service;

import pmsoft.sam.module.definition.architecture.AbstractSamServiceDefinition;

public class PhotoSharingServiceDefinition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(PhotoSharingAPI.class);
	}

}
