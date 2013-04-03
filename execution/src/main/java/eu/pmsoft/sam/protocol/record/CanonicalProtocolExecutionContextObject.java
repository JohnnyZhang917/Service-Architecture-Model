package eu.pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.execution.ThreadMessagePipe;
import eu.pmsoft.sam.protocol.CanonicalProtocolExecutionContext;
import eu.pmsoft.sam.protocol.TransactionController;

import java.net.URL;
import java.util.List;
import java.util.UUID;

class CanonicalProtocolExecutionContextObject implements CanonicalProtocolExecutionContext {

    private final MethodRecordContext recordStack;
    private final MethodRecordContext executionStack;
    private final UUID canonicalTransactionIdentificator;
    private final TransactionController controller;
    private final ImmutableList<URL> endpointAddresses;
    private final Injector headTransactionInjector;

    @Inject
    public CanonicalProtocolExecutionContextObject(@Assisted("record") MethodRecordContext recordStack, @Assisted("execute") MethodRecordContext executionStack,
                                                   @Assisted UUID canonicalTransactionIdentificator, @Assisted TransactionController controller, @Assisted ImmutableList<URL> endpointAddresses,
                                                   @Assisted InjectorReference glueInjector) {
        this.recordStack = recordStack;
        this.executionStack = executionStack;
        this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;
        this.controller = controller;
        this.endpointAddresses = endpointAddresses;
        this.headTransactionInjector = glueInjector.getInjector();
    }

    @Override
    public UUID getContextUniqueID() {
        return canonicalTransactionIdentificator;
    }

    @Override
    public TransactionController getTransactionController() {
        return controller;
    }

    @Override
    public Injector getInjector() {
        return headTransactionInjector;
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        return headTransactionInjector.getInstance(key);
    }

    @Override
    public void enterExecution(ThreadMessagePipe headCommandPipe, List<ThreadMessagePipe> endpoints) {
        controller.enterTransactionContext();
        if (headCommandPipe != null) {
            this.executionStack.getExecutionManager().bindExecutionPipes(ImmutableList.of(headCommandPipe));
        }
        this.recordStack.getExecutionManager().bindExecutionPipes(ImmutableList.copyOf(endpoints));
    }

    @Override
    public void exitExecution() {
        controller.exitTransactionContext();
    }

    @Override
    public List<URL> getEndpointAdressList() {
        return endpointAddresses;
    }

    @Override
    public void executeCanonicalProtocol() {
        this.executionStack.getExecutionManager().executeCanonicalProtocol();
    }

    @Override
    public void initTransaction() {
        this.executionStack.getExecutionManager().bindTransaction();
        this.recordStack.getExecutionManager().bindTransaction();
    }

    @Override
    public void closeTransaction() {
        this.recordStack.getExecutionManager().unbindPipe();
        this.recordStack.getExecutionManager().unbindTransaction();
        this.executionStack.getExecutionManager().unbindTransaction();
    }
}
