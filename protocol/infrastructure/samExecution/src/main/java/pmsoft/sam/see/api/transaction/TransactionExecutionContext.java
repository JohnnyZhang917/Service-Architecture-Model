package pmsoft.sam.see.api.transaction;

import pmsoft.sam.protocol.injection.TransactionController;

import com.google.inject.Injector;

public interface TransactionExecutionContext {
	
	public TransactionController getTransactionController();

	public Injector getInjector();
}
