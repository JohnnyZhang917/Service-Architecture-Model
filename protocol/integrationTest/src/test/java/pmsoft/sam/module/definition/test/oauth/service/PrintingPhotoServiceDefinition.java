package pmsoft.sam.module.definition.test.oauth.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class PrintingPhotoServiceDefinition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(PrintingPhotoAPI.class);
	}

}
