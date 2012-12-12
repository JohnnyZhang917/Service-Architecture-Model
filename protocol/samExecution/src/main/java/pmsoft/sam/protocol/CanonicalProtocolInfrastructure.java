package pmsoft.sam.protocol;

import java.util.UUID;

import pmsoft.sam.see.api.model.SamInstanceTransaction;

public interface CanonicalProtocolInfrastructure {

	public CanonicalProtocolExecutionContext createExecutionContext(final SamInstanceTransaction functionContract);

	public CanonicalProtocolExecutionContext bindExecutionContext(SamInstanceTransaction transaction, UUID transactionUniqueId);

}
