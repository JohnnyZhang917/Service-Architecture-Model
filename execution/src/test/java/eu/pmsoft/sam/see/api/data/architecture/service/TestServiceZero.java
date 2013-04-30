package eu.pmsoft.sam.see.api.data.architecture.service;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.sam.see.api.data.architecture.contract.TestInterfaceZero;

public class TestServiceZero extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        withKey(TestInterfaceZero.class);
    }

}
