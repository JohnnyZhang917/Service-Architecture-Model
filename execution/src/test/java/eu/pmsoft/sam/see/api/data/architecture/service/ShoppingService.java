package eu.pmsoft.sam.see.api.data.architecture.service;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;

public class ShoppingService extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        addInterface(ShoppingStoreWithCourierInteraction.class);
    }

}
