package eu.pmsoft.ibus.example.architecture.information.authentication;

import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;

public class CitizenIdentityService extends AbstractSamServiceDefinition {

    @Override
    public void loadServiceDefinition() {
        addInterface(CitizenAuthenticationService.class);
        addInterface(CitizenPresentationService.class);
    }
}
