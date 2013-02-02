package pmsoft.sam.see.api.data.architecture.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceTwo0;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceTwo1;

public class TestServiceTwo extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        addInterface(TestInterfaceTwo0.class);
        addInterface(TestInterfaceTwo1.class);
    }

}
