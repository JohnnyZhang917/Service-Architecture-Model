package pmsoft.sam.see.api.data.architecture;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class TestServiceZero extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(TestInterfaceZero.class);
	}

}
