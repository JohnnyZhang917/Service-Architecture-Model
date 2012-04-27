package pmsoft.sam.module.definition.test.data.service;

import pmsoft.sam.module.definition.architecture.AbstractSamServiceDefinition;

public class Service3Definition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(Service3a.class);
	}

}
