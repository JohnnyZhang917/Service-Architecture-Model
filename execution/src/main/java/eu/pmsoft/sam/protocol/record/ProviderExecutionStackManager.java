package eu.pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.execution.ThreadMessage;
import eu.pmsoft.execution.ThreadMessagePipe;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.CanonicalRequest;
import eu.pmsoft.sam.protocol.transport.data.CanonicalProtocolRequestData;
import eu.pmsoft.sam.see.api.model.ExecutionStrategy;
import org.slf4j.LoggerFactory;

import java.util.List;

class ProviderExecutionStackManager extends AbstractExecutionStackManager {

    @Inject
    public ProviderExecutionStackManager(@Assisted ImmutableList<InstanceRegistry> instanceRegistries, @Assisted ExecutionStrategy executionStrategy) {
        super(instanceRegistries, LoggerFactory.getLogger(ProviderExecutionStackManager.class), executionStrategy);
    }

    @Override
    public void executeCanonicalProtocol() {
        assert instanceRegistries.size() == 1 : "execution instance expected";
        assert pipes != null : "head pipe not bind";
        assert pipes.size() == 1 : "more that one head pipe";
        ThreadMessagePipe headPipe = pipes.get(0);
        InstanceRegistry executionRegistry = instanceRegistries.get(0);
        logger.trace("starting provider execution");
        ThreadMessage protocolMessage = headPipe.waitResponse();
        CanonicalProtocolRequestData data = new CanonicalProtocolRequestData(protocolMessage.getPayload());
        List<CanonicalInstanceReference> instanceReferences = data.getInstanceReferences();
        mergeInstances(executionRegistry, instanceReferences);
        executeCalls(data.getMethodCalls(), executionRegistry);
        flushExecution();
        List<CanonicalInstanceReference> instanceReferenceToTransfer = executionRegistry.getInstanceReferenceToTransfer();
        ThreadMessage message = new ThreadMessage();
        if (!instanceReferenceToTransfer.isEmpty()) {
            //instanceReferenceToTransfer, null, true
            CanonicalRequest responseData = new CanonicalRequest();
            responseData.setInstancesList(instanceReferenceToTransfer);
            responseData.setCloseThread(true);
            CanonicalProtocolRequestData closingData = new CanonicalProtocolRequestData(responseData);
            message.setPayload(closingData.getPayload());
        }
        headPipe.sendMessage(message);
        logger.trace("provider execution done");
    }

}
