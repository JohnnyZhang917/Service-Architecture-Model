package pmsoft.sam.see.api.data.architecture.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceZero;

public class TestServiceZero extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(TestInterfaceZero.class);
	}

}
