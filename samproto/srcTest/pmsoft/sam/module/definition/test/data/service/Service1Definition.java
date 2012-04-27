package pmsoft.sam.module.definition.test.data.service;

import pmsoft.sam.module.definition.architecture.AbstractSamServiceDefinition;

public class Service1Definition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(Service1a.class);
	}

}
