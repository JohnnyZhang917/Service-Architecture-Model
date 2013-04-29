package eu.pmsoft.execution;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import eu.pmsoft.sam.see.api.model.STID;
import eu.pmsoft.sam.see.transport.SamTransportChannel;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadExecutionContext {
    private final AtomicBoolean globalTransactionInitialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ThreadExecutionContextInternalLogic internalLogic;
//    private final ThreadMessagePipe headCommandPipe;
    private final UUID transactionID;
    @InjectLogger
    private Logger logger;
    private List<SamTransportChannel> clientConnectionChannels;

    @Inject
    ThreadExecutionContext(@Assisted ThreadExecutionContextInternalLogic internalLogic,
//                           @Nullable @Assisted ThreadMessagePipe headCommandPipe,
                           @Assisted UUID transactionID) {
        super();
//        this.headCommandPipe = headCommandPipe;
        this.internalLogic = internalLogic;
        this.transactionID = transactionID;
    }

    public ThreadExecutionContextInternalLogic getInternalLogic() {
        return internalLogic;
    }

    public void initializeClientConnectionChannels(List<SamTransportChannel> clientConnectionChannels) {
        assert this.clientConnectionChannels == null;
        this.clientConnectionChannels = clientConnectionChannels;
        assert this.clientConnectionChannels != null;
    }



    public UUID getTransactionID() {
        return transactionID;
    }

//    ThreadMessagePipe getHeadCommandPipe() {
//        return this.headCommandPipe;
//    }


    <T> T getInstance(Key<T> key) {
        return internalLogic.getInstance(key);
    }

    public <R, T> R executeInteraction(ServiceAction<R, T> interaction) {
        assert !globalTransactionInitialized.get();
        T instance = getInstance(interaction.getInterfaceKey());
        return interaction.executeInteraction(instance);
    }

    public void executeCanonicalProtocol() {
        internalLogic.executeCanonicalProtocol();
    }

    public boolean enterExecutionContext() {
        if (running.compareAndSet(false, true)) {
//            internalLogic.enterExecution(headCommandPipe, endpoints);
            internalLogic.enterExecution();
            return true;
        }
        return false;
    }

    public void exitTransactionContext() {
        if (running.compareAndSet(true, false)) {
            internalLogic.exitExecution();
        }
    }

    boolean isExecutionRunning() {
        return running.get();
    }

    public void initGrobalTransactionContext(UUID transactionId) {
        internalLogic.initTransaction();
//        for (int i = 0; i < endpoints.size(); i++) {
//            ThreadMessagePipe pipe = endpoints.get(i);
//            pipe.initializeTransactionConnection();
//        }
    }

    public void exitGrobalTransactionContext() {
//        for (int i = 0; i < endpoints.size(); i++) {
//            ThreadMessagePipe pipe = endpoints.get(i);
//            pipe.closeTransactionConnection();
//        }
        internalLogic.closeTransaction();

    }

}
