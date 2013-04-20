package eu.pmsoft.execution;

import com.google.inject.Inject;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class ThreadExecutionManager {

    private final ExecutorService pool;
    private final ExecutionContextManager contextManager;
    @InjectLogger
    private Logger logger;

    @Inject
    ThreadExecutionManager(ExecutorService pool, ExecutionContextManager contextManager) {
        this.pool = pool;
        this.contextManager = contextManager;
    }

    <R, T> Future<R> executeServiceAction(ServiceAction<R, T> interaction) {
        UUID executionContextID = contextManager.createExecutionContextForServiceInteraction(interaction.getServiceURL());
        return pool.submit(new ServiceInteractionThreadWrapper<R, T>(interaction, contextManager.getServiceInteractionContext(executionContextID)));
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
                executionContext.initGrobalTransactionContext();
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
