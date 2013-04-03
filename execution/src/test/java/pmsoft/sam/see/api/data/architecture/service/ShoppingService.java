package pmsoft.sam.see.api.data.architecture.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;

public class ShoppingService extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        addInterface(ShoppingStoreWithCourierInteraction.class);
    }

}
