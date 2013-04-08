package eu.pmsoft.ibus.example.architecture.citizen.control;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class CitizenControlAccessService extends AbstractSamServiceDefinition {
    @Override
    public void loadServiceDefinition() {
        addInterface(CitizenAuthentication.class);
        addInterface(CitizenManagement.class);
    }
}
