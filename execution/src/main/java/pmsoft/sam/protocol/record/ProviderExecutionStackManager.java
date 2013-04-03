package pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.LoggerFactory;
import pmsoft.execution.ThreadMessage;
import pmsoft.execution.ThreadMessagePipe;
import pmsoft.sam.protocol.transport.data.AbstractInstanceReference;
import pmsoft.sam.protocol.transport.data.CanonicalProtocolRequestData;
import pmsoft.sam.see.api.model.ExecutionStrategy;

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
        CanonicalProtocolRequestData data = (CanonicalProtocolRequestData) protocolMessage.getPayload();
        List<AbstractInstanceReference> instanceReferences = data.getInstanceReferences();
        mergeInstances(executionRegistry, instanceReferences);
        executeCalls(data.getMethodCalls(), executionRegistry);
        flushExecution();
        List<AbstractInstanceReference> instanceReferenceToTransfer = executionRegistry.getInstanceReferenceToTransfer();
        ThreadMessage message = new ThreadMessage();
        if (!instanceReferenceToTransfer.isEmpty()) {
            CanonicalProtocolRequestData closingData = new CanonicalProtocolRequestData(instanceReferenceToTransfer, null, true);
            message.setPayload(closingData);
        }
        headPipe.sendMessage(message);
        logger.trace("provider execution done");
    }

}
