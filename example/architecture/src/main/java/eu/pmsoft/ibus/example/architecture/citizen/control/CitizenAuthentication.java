package eu.pmsoft.ibus.example.architecture.citizen.control;

import eu.pmsoft.ibus.example.architecture.citizen.AutenticationSessionToken;

public interface CitizenAuthentication {

    public String getFirstTokenStep(String credentials);

    public AutenticationSessionToken closeAutenticationTransaction(String token, String signResponse);
}
