package eu.pmsoft.sam.protocol;

import com.google.inject.Injector;
import eu.pmsoft.execution.ThreadExecutionContextInternalLogic;

import java.util.UUID;

public interface CanonicalProtocolThreadExecutionContext extends ThreadExecutionContextInternalLogic {

    public UUID getContextUniqueID();

    public TransactionController getTransactionController();

    public Injector getInjector();

}
