package pmsoft.sam.see.api.data.architecture.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceOne;

public class TestServiceOne extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(TestInterfaceOne.class);
	}

}
