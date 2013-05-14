package eu.pmsoft.sam.see;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import eu.pmsoft.see.api.SamExecutionEnvironment;
import eu.pmsoft.see.api.infrastructure.SamExecutionEnvironmentInfrastructure;
import eu.pmsoft.see.api.setup.SamExecutionNode;
import eu.pmsoft.see.api.setup.SamExecutionNodeInternalApi;
import eu.pmsoft.sam.see.transport.SamTransportCommunicationContext;
import eu.pmsoft.sam.see.transport.SamTransportLayer;

import java.util.concurrent.ConcurrentMap;

public class SamExecutionEnvironmentSimple implements SamExecutionEnvironment {

    private final SamExecutionEnvironmentInfrastructure environmentInfrastructure;
    private final SamTransportLayer transportLayer;
    private final Provider<SamExecutionNodeInternalApi> executionNodeProvider;
    private final ConcurrentMap<Integer, SamExecutionNodeInternalApi> nodeExecutionMap = Maps.newConcurrentMap();

    @Inject
    public SamExecutionEnvironmentSimple(SamExecutionEnvironmentInfrastructure environmentInfrastructure, SamTransportLayer transportLayer, Provider<SamExecutionNodeInternalApi> executionNodeProvider) {
        this.environmentInfrastructure = environmentInfrastructure;
        this.transportLayer = transportLayer;
        this.executionNodeProvider = executionNodeProvider;
    }

    @Override
    public SamExecutionEnvironmentInfrastructure getInfrastructureApi() {
        return environmentInfrastructure;
    }

    @Override
    public SamExecutionNode createExecutionNode(int port) {
        if (!nodeExecutionMap.containsKey(port)) {
            SamExecutionNodeInternalApi newNode = internalNodeCreation(port);
            nodeExecutionMap.putIfAbsent(port, newNode);
        }
        return nodeExecutionMap.get(port);
    }

    private SamExecutionNodeInternalApi internalNodeCreation(int port) {
        SamExecutionNodeInternalApi samExecutionNode = executionNodeProvider.get();
        SamTransportCommunicationContext executionEndpoint = transportLayer.createExecutionEndpoint(port);
        samExecutionNode.bindToServer(executionEndpoint);
        return samExecutionNode;
    }
}
