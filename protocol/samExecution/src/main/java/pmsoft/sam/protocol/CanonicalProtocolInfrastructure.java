package pmsoft.sam.protocol;

import pmsoft.sam.see.api.model.SamInstanceTransaction;

import java.util.UUID;

public interface CanonicalProtocolInfrastructure {

	public CanonicalProtocolExecutionContext createExecutionContext(final SamInstanceTransaction functionContract);

	public CanonicalProtocolExecutionContext bindExecutionContext(SamInstanceTransaction transaction, UUID transactionUniqueId);

}
