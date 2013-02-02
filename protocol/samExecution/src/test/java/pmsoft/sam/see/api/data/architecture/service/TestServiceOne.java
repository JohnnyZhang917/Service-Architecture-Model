package pmsoft.sam.see.api.data.architecture.service;

import com.google.inject.name.Names;
import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceOne;

import java.lang.annotation.Annotation;

public class TestServiceOne extends AbstractSamServiceDefinition {

    public static final Annotation TEST_NAME = Names.named("test1");

    @Override
    public void loadServiceDefinition() {
        addInterface(TestInterfaceOne.class);
        addInterface(TestInterfaceOne.class, TEST_NAME);
    }

}
