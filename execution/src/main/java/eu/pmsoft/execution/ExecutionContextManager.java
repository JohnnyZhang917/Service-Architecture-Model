package eu.pmsoft.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.netty.channel.Channel;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

class ExecutionContextManager {
    private final ConcurrentMap<UUID, ThreadExecutionContext> localExecutionMap;
    private final ConcurrentMap<String, ThreadExecutionContext> externalExecutionMap;
    private final ThreadExecutionLoginProvider internalLogicFactory;
    private final ThreadExecutionModelFactory factory;
    private final ThreadConnectionManager connectionManager;

    @Inject
    ExecutionContextManager(ThreadExecutionLoginProvider internalLogicFactory, ThreadExecutionModelFactory factory, ThreadConnectionManager connectionManager) {
        super();
        this.localExecutionMap = Maps.newConcurrentMap();
        this.externalExecutionMap = Maps.newConcurrentMap();
        this.internalLogicFactory = internalLogicFactory;
        this.factory = factory;
        this.connectionManager = connectionManager;
    }

    UUID createExecutionContextForServiceInteraction(URL transactionURL) {
        return openExecutionContextForServiceInteraction(UUID.randomUUID(), transactionURL).getTransactionID();
    }

    ThreadExecutionContext openExecutionContextForServiceInteraction(UUID transactionID, URL transactionURL) {
        Preconditions.checkNotNull(transactionID);
        Preconditions.checkNotNull(transactionURL);
        if (!localExecutionMap.containsKey(transactionID)) {
            ThreadExecutionContext context = createExecutionContext(null, transactionID, transactionURL, null);
            localExecutionMap.putIfAbsent(transactionID, context);
        }
        return localExecutionMap.get(transactionID);
    }

    ThreadExecutionContext openExecutionContextForProtocolExecution(String headPipeSignature, UUID transactionID, URL transactionURL, Channel channel) {
        Preconditions.checkNotNull(headPipeSignature);
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(transactionID);
        Preconditions.checkNotNull(transactionURL);
        return internalOpenExecutionContext(headPipeSignature, transactionID, transactionURL, channel);

    }

    private ThreadExecutionContext internalOpenExecutionContext(String headPipeSignature, UUID transactionID, URL transactionURL, Channel channel) {
        if (!externalExecutionMap.containsKey(headPipeSignature)) {
            ThreadExecutionContext context = createExecutionContext(headPipeSignature, transactionID, transactionURL, channel);
            externalExecutionMap.putIfAbsent(headPipeSignature, context);
        }
        return externalExecutionMap.get(headPipeSignature);
    }


    private ThreadExecutionContext createExecutionContext(String headPipeSignature, UUID transactionID, URL transactionURL, Channel channel) {
        ThreadExecutionContextInternalLogic internalLogic = internalLogicFactory.open(transactionURL);
        ThreadMessagePipe headPipe = null;
        if (channel != null) {
            headPipe = connectionManager.createHeadPipe(headPipeSignature, channel);
        }
        List<ThreadMessagePipe> endpoints = connectionManager.openPipes(transactionID, internalLogic.getEndpointAddressList());
        ThreadExecutionContext context = factory.threadExecutionContext(internalLogic, headPipe, endpoints, transactionID);
        return context;
    }

    public ThreadExecutionContext getServiceInteractionContext(UUID executionContextID) {
        return localExecutionMap.get(executionContextID);
    }
}
