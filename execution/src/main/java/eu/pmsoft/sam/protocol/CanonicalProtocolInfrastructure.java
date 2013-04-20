package eu.pmsoft.sam.protocol;

import eu.pmsoft.sam.see.api.model.SamInstanceTransaction;

import java.util.UUID;

public interface CanonicalProtocolInfrastructure {

    public CanonicalProtocolThreadExecutionContext createExecutionContext(final SamInstanceTransaction functionContract);

    public CanonicalProtocolThreadExecutionContext bindExecutionContext(SamInstanceTransaction transaction, UUID transactionUniqueId);

}
