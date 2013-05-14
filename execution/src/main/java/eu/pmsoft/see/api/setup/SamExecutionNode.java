package eu.pmsoft.see.api.setup;

import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.see.api.model.*;

import java.util.Map;
import java.util.Set;

public interface SamExecutionNode {


    /**
     * Returns all Service Instance created from the service implementation
     * identified by key and matching metadata
     *
     * @param key      Service Implementation used to create the service Instances
     * @param metadata Required matching Metadata
     * @return ServiceInstances that match the given key and metadata
     */
    public Set<SamServiceInstance> searchInstance(SamServiceImplementationKey key, ServiceMetadata metadata);


    /**
     * Publish to service Transaction on the ServiceDiscovery infrastructure
     * binded to the ExecutionNode
     *
     * @param url URL of the transaction
     */
    public SamServiceInstanceTransaction getTransaction(STID url);

//    public void setupConfiguration(SEENodeConfiguration nodeConfiguration);

    public Map<STID, ServiceKeyDeprecated> getServiceTransactionSetup();

    public SIURL exposeInjectionConfiguration(STID stid);

//    public SamExecutionApi getExecutionApi();
}
