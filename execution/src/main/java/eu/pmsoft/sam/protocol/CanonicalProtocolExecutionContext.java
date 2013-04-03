package eu.pmsoft.sam.protocol;

import com.google.inject.Injector;
import eu.pmsoft.execution.ExecutionContextInternalLogic;

import java.util.UUID;

public interface CanonicalProtocolExecutionContext extends ExecutionContextInternalLogic {

    public UUID getContextUniqueID();

    public TransactionController getTransactionController();

    public Injector getInjector();

}
