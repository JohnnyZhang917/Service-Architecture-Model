package pmsoft.sam.module.definition.test.oauth.service;

import pmsoft.sam.module.definition.architecture.AbstractSamServiceDefinition;

public class JaneGuiServiceDefinition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(JaneGuiService.class);
	}

}
