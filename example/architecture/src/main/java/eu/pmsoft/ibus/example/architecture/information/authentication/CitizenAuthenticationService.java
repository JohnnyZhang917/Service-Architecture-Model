package eu.pmsoft.ibus.example.architecture.information.authentication;

import eu.pmsoft.ibus.example.architecture.information.CitizenIdentityManagementParameters;
import eu.pmsoft.ibus.example.architecture.information.CitizenRegistrationMessage;
import eu.pmsoft.ibus.example.architecture.information.CitizenRegistrationProcessState;

public interface CitizenAuthenticationService {

    public CitizenRegistrationProcessState requestCitizenIdentityCreation(CitizenRegistrationMessage registration);

    public CitizenRegistrationProcessState verifyRegistrationState(CitizenRegistrationProcessState state);

    public CitizenIdentityManagementParameters getCitizenIdentityParameters(Long citizenIID);

    public CitizenIdentityManagementParameters[] getCitizenIdentityParameters(Long[] citizenIIDArray);

    public boolean updateCitizenIdentityParameters(CitizenIdentityManagementParameters parameters) throws AuthorizationAccessException;

}
