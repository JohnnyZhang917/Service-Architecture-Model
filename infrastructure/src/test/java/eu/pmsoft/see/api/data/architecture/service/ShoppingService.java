package eu.pmsoft.see.api.data.architecture.service;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;

public class ShoppingService extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        withKey(ShoppingStoreWithCourierInteraction.class);
    }

}
