package eu.pmsoft.sam.see.transport;

import eu.pmsoft.see.api.model.SIURL;
import eu.pmsoft.see.api.model.STID;

import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface SamTransportCommunicationContext {

    SIURL liftServiceTransaction(STID stid);

    List<SamTransportChannel> createClientConnectionChannels(UUID transactionID, List<URL> endpointAddressList);
}
