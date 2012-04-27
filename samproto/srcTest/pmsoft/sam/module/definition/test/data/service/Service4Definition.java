package pmsoft.sam.module.definition.test.data.service;

import pmsoft.sam.module.definition.architecture.AbstractSamServiceDefinition;

public class Service4Definition extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(Service4a.class);
	}

}
