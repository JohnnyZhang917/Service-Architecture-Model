package pmsoft.sam.module.definition.test.data.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class Service2Definition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(Service2a.class);
		addInterface(Service2b.class);
	}


}
