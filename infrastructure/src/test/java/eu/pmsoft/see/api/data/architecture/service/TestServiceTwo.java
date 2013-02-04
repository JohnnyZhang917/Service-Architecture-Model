package eu.pmsoft.see.api.data.architecture.service;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceTwo0;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceTwo1;

public class TestServiceTwo extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        withKey(TestInterfaceTwo0.class);
        withKey(TestInterfaceTwo1.class);
    }

}
