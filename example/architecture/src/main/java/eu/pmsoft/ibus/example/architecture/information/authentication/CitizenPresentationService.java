package eu.pmsoft.ibus.example.architecture.information.authentication;

import eu.pmsoft.ibus.example.architecture.information.*;

public interface CitizenPresentationService {

    public PresentationResponse createPresentationToken(PresentationRequest presentation);

    public boolean validatePresentationToken( CitizenPresentationToken token);
}
