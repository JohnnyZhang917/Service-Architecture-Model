package pmsoft.sam.protocol.injection;

import java.util.UUID;

import com.google.inject.Injector;

public interface CanonicalProtocolExecutionContext extends CanonicalProtocolRequestHandler {

	public UUID getContextUniqueID();

	public TransactionController getTransactionController();

	public Injector getInjector();
}
