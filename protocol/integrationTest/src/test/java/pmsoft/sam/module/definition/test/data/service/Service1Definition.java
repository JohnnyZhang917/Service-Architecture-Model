package pmsoft.sam.module.definition.test.data.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class Service1Definition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(Service1a.class);
	}

}
