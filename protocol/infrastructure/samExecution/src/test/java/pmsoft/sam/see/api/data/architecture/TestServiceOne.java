package pmsoft.sam.see.api.data.architecture;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class TestServiceOne extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(TestInterfaceOne.class);
	}

}
