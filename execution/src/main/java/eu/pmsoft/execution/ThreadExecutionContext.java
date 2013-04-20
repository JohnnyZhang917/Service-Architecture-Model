package eu.pmsoft.execution;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

class ThreadExecutionContext {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ThreadExecutionContextInternalLogic internalLogic;
    // Head pipe is null for execution context directly connected to service interactions on local server
    private final ThreadMessagePipe headCommandPipe;
    private final List<ThreadMessagePipe> endpoints;
    private final UUID transactionID;
    @InjectLogger
    private Logger logger;

    @Inject
    ThreadExecutionContext(@Assisted ThreadExecutionContextInternalLogic internalLogic, @Nullable @Assisted ThreadMessagePipe headCommandPipe,
                           @Assisted List<ThreadMessagePipe> endpoints, @Assisted UUID transactionID) {
        super();
        this.internalLogic = internalLogic;
        this.headCommandPipe = headCommandPipe;
        this.endpoints = endpoints;
        this.transactionID = transactionID;
    }

    public UUID getTransactionID() {
        return transactionID;
    }

    ThreadMessagePipe getHeadCommandPipe() {
        return this.headCommandPipe;
    }


    <T> T getInstance(Key<T> key) {
        return internalLogic.getInstance(key);
    }

    <R, T> R executeInteraction(ServiceAction<R, T> interaction) {
        T instance = getInstance(interaction.getInterfaceKey());
        return interaction.executeInteraction(instance);
    }

    void executeCanonicalProtocol() {
        internalLogic.executeCanonicalProtocol();
    }

    boolean enterExecutionContext() {
        if (running.compareAndSet(false, true)) {
            internalLogic.enterExecution(headCommandPipe, endpoints);
            return true;
        }
        return false;
    }

    void exitTransactionContext() {
        if (running.compareAndSet(true, false)) {
            internalLogic.exitExecution();
        }
    }

    boolean isExecutionRunning() {
        return running.get();
    }

    public void initGrobalTransactionContext() {
        internalLogic.initTransaction();
        for (int i = 0; i < endpoints.size(); i++) {
            ThreadMessagePipe pipe = endpoints.get(i);
            pipe.initializeTransactionConnection();
        }
    }

    public void exitGrobalTransactionContext() {
        for (int i = 0; i < endpoints.size(); i++) {
            ThreadMessagePipe pipe = endpoints.get(i);
            pipe.closeTransactionConnection();
        }
        internalLogic.closeTransaction();

    }
}
