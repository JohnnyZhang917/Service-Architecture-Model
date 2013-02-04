package eu.pmsoft.see.api.data.architecture.service;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceContract;

public class CourierService extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        // Only exposed interfaces are defined in the service contractKeys
        withKey(CourierServiceContract.class);
    }

}
