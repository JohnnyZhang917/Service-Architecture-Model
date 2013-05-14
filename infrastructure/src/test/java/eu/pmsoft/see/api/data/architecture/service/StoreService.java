package eu.pmsoft.see.api.data.architecture.service;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreServiceContract;

public class StoreService extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        withKey(StoreServiceContract.class);
    }

}
