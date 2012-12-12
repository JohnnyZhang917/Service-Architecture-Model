package pmsoft.sam.protocol;

import java.util.UUID;

import pmsoft.execution.ExecutionContextInternalLogic;

import com.google.inject.Injector;

public interface CanonicalProtocolExecutionContext extends ExecutionContextInternalLogic {

	public UUID getContextUniqueID();

	public TransactionController getTransactionController();

	public Injector getInjector();

}
