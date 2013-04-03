package eu.pmsoft.sam.service.user;

import eu.pmsoft.sam.ds.DomainData;
import eu.pmsoft.sam.ds.DomainTypeExample;

public interface UserDataComplexAPI {

    void clientInformation(int data);

    boolean finalizeComplexClientInteraction();

    DomainTypeExample getDomainData(DomainData data);
}
