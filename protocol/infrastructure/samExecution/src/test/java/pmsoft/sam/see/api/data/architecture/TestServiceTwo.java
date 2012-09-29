package pmsoft.sam.see.api.data.architecture;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class TestServiceTwo extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(TestInterfaceTwo0.class);
		addInterface(TestInterfaceTwo1.class);
	}

}
