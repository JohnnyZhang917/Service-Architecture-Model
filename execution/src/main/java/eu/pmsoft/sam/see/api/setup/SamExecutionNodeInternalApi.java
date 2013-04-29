package eu.pmsoft.sam.see.api.setup;

import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.see.api.model.*;
import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.sam.see.transport.SamTransportCommunicationContext;

import java.util.UUID;

public interface SamExecutionNodeInternalApi extends SamExecutionNode {

    public SamServiceInstance getInternalServiceInstance(SIID exposedService);

    void bindToServer(SamTransportCommunicationContext executionEndpointServer);

    SamTransportCommunicationContext getNodeTransportServer();


    /**
     * Create a new instance of the given implementation with provided metadata.
     *
     * @param key      the service implementation key as registered on the
     *                 ServiceRegistry
     * @param metadata Metadata instance for the new Service Instance, null is
     *                 interpreted as empty metadata
     * @return the new ServiceInstance reference
     */
    public SamServiceInstance createServiceInstance(SamServiceImplementationKey key, ServiceMetadata metadata);

    public STID setupInjectionTransaction(SamInjectionConfiguration configuration);


    public CanonicalProtocolThreadExecutionContext createTransactionExecutionContext(STID url);

    public CanonicalProtocolThreadExecutionContext openTransactionExecutionContext(STID targetUrl, UUID transactionUniqueId);

}
