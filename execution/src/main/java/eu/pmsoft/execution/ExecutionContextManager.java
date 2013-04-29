package eu.pmsoft.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.see.api.model.STID;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class ExecutionContextManager {
    private final ConcurrentMap<UUID, ThreadExecutionContext> localExecutionMap;
    private final ConcurrentMap<String, ThreadExecutionContext> externalExecutionMap;
//    private final ThreadExecutionLogicProvider internalLogicFactory;
    private final ThreadExecutionModelFactory factory;
//    private final ThreadConnectionManager connectionManager;

    @Inject
    ExecutionContextManager(ThreadExecutionModelFactory factory) {
        super();
        this.localExecutionMap = Maps.newConcurrentMap();
        this.externalExecutionMap = Maps.newConcurrentMap();
//        this.internalLogicFactory = internalLogicFactory;
        this.factory = factory;
//        this.connectionManager = connectionManager;
    }

    public ThreadExecutionContext createExecutionContextForServiceInteraction(ThreadExecutionContextInternalLogic threadExecutionInternalLogic, UUID transactionID) {
        Preconditions.checkNotNull(threadExecutionInternalLogic);
        ThreadExecutionContext context = createExecutionContext(transactionID, threadExecutionInternalLogic);
        localExecutionMap.put(transactionID,context);
        return localExecutionMap.get(transactionID);
    }

    // TODO FIXME
    ThreadExecutionContext openExecutionContextForProtocolExecution(UUID transactionID, STID transactionURL) {
        Preconditions.checkNotNull(transactionID);
        Preconditions.checkNotNull(transactionURL);
        return internalOpenExecutionContext(transactionID, transactionURL);

    }

    private ThreadExecutionContext internalOpenExecutionContext(UUID transactionID, STID transactionURL) {
        throw new RuntimeException("TODO REFACTOR");
//        String headPipeSignature = transactionID.toString() + transactionURL.toString();
//        if (!externalExecutionMap.containsKey(headPipeSignature)) {
//            ThreadExecutionContext context = createExecutionContext(transactionID, transactionURL);
//            externalExecutionMap.putIfAbsent(headPipeSignature, context);
//        }
//        return externalExecutionMap.get(headPipeSignature);
    }


    private ThreadExecutionContext createExecutionContext(UUID transactionID, ThreadExecutionContextInternalLogic internalLogic) {
        assert internalLogic != null;
        assert transactionID != null;
//        ThreadExecutionContextInternalLogic internalLogic = internalLogicFactory.open(transactionURL);
//        ThreadMessagePipe headPipe = null;
//        if (channel != null) {
//            headPipe = connectionManager.createHeadPipe(headPipeSignature, channel);
//        }
//        List<ThreadMessagePipe> endpoints = connectionManager.openPipes(transactionID, internalLogic.getEndpointAddressList());
        ThreadExecutionContext context = factory.threadExecutionContext(internalLogic,
//                headPipe, endpoints,
                transactionID);
        return context;
    }

    public ThreadExecutionContext getServiceInteractionContext(UUID executionContextID) {
        return localExecutionMap.get(executionContextID);
    }
}
