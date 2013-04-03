package eu.pmsoft.sam.see.api.model;

import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.sam.see.api.transaction.SamInjectionModelVisitor;

public interface SamInstanceTransaction {

    public ExecutionStrategy getExecutionStrategy();

    public SIURL getTransactionURL();

    public SamInjectionConfiguration getInjectionConfiguration();

    public <T> T accept(SamInjectionModelVisitor<T> visitor);

}