package eu.pmsoft.sam.see.execution.localjvm;

import eu.pmsoft.execution.ExecutionContextManager;
import eu.pmsoft.execution.ServiceAction;
import eu.pmsoft.execution.ThreadExecutionContext;
import eu.pmsoft.execution.ThreadExecutionContextInternalLogic;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.see.api.model.SamServiceImplementationKey;
import eu.pmsoft.sam.see.api.model.SamServiceInstance;
import eu.pmsoft.sam.see.api.model.ServiceMetadata;
import eu.pmsoft.sam.see.api.setup.SamExecutionNodeInternalApi;
import eu.pmsoft.sam.see.transport.SamTransportChannel;
import eu.pmsoft.sam.see.transport.SamTransportCommunicationContext;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SamExecutionApiJVM {

    private final ExecutorService pool;
    private final ExecutionContextManager contextManager;
        private final SamExecutionNodeInternalApi nodeInternalApi;
    @InjectLogger
    private Logger logger;

    @Inject
    public SamExecutionApiJVM(ExecutorService pool, ExecutionContextManager contextManager, SamExecutionNodeInternalApi nodeInternalApi) {
        this.pool = pool;
        this.contextManager = contextManager;
        this.nodeInternalApi = nodeInternalApi;
    }


//    @Override
    public <R, T> Future<R> executeServiceAction(ServiceAction<R, T> interaction) {
        CanonicalProtocolThreadExecutionContext transactionExecutionContext = nodeInternalApi.createTransactionExecutionContext(interaction.getServiceURL());
        SamTransportCommunicationContext nodeTransportServer = nodeInternalApi.getNodeTransportServer();

        ThreadExecutionContext executionContextForServiceInteraction = contextManager.createExecutionContextForServiceInteraction(transactionExecutionContext, transactionExecutionContext.getContextUniqueID());


        ThreadExecutionContextInternalLogic internalLogic = executionContextForServiceInteraction.getInternalLogic();
        List<SamTransportChannel> clientConnectionChannels = nodeTransportServer.createClientConnectionChannels(executionContextForServiceInteraction.getTransactionID(), internalLogic.getEndpointAddressList());

        executionContextForServiceInteraction.initializeClientConnectionChannels(clientConnectionChannels);

        return pool.submit(new ServiceInteractionThreadWrapper<R, T>(interaction, executionContextForServiceInteraction));
    }


    void runExecutionContext(ThreadExecutionContext context) {
        pool.submit(new TransactionExecutionThreadWrapper(context));
    }

    private class ServiceInteractionThreadWrapper<R, T> implements Callable<R> {
        private final ServiceAction<R, T> interaction;
        private final ThreadExecutionContext executionContext;

        ServiceInteractionThreadWrapper(ServiceAction<R, T> interaction, ThreadExecutionContext executionContext) {
            super();
            this.interaction = interaction;
            this.executionContext = executionContext;
        }

        @Override
        public R call() throws Exception {
            logger.trace("execution thread running for service interaction");
            try {
                executionContext.initGrobalTransactionContext(null);
                if (executionContext.enterExecutionContext()) {
                    logger.trace("execution is open");
                    logger.trace("enter to service interaction");
                    return executionContext.executeInteraction(interaction);
                }
            } catch (Exception e) {
                logger.error("execution exceptions", e);
            } finally {
                executionContext.exitTransactionContext();
                executionContext.exitGrobalTransactionContext();
                logger.trace("execution closed");
            }
            // this should not happen
            logger.error("it was not possible to open the transaction for service interaction. Fatal exceptions");
            throw new RuntimeException("fatal exceptions on transaction open for service interaction");
        }
    }

    private class TransactionExecutionThreadWrapper implements Callable<Void> {
        private final ThreadExecutionContext executionContext;

        TransactionExecutionThreadWrapper(ThreadExecutionContext executionContext) {
            super();
            this.executionContext = executionContext;
        }

        @Override
        public Void call() throws Exception {
            logger.trace("execution thread running for canonical protocol execution");
            try {
                if (executionContext.enterExecutionContext()) {
                    logger.trace("execution is open");
                    logger.trace("start canonical protocol execution");
                    executionContext.executeCanonicalProtocol();
                    return null;
                }
            } catch (Exception e) {
                logger.error("execution exceptions", e);   // FIXME exception policy.
                // TODO expand exceptions to global transaction context
            } finally {
                executionContext.exitTransactionContext();
                logger.trace("execution closed");
            }
            logger.info("execution already open, ignoring duplicate execution start");
            // FIXME exception policy.
            return null;
        }
    }

}

