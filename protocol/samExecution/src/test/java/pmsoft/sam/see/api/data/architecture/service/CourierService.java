package pmsoft.sam.see.api.data.architecture.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceContract;

public class CourierService extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        // Only exposed interfaces are defined in the service contract
        addInterface(CourierServiceContract.class);
    }

}
