package pmsoft.sam.module.definition.test.data.service;

import pmsoft.sam.module.definition.architecture.AbstractSamServiceDefinition;

public class Service2Definition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(Service2a.class);
		addInterface(Service2b.class);
	}


}
