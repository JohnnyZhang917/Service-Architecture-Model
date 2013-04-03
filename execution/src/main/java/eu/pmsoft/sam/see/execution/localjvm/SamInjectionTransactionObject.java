package eu.pmsoft.sam.see.execution.localjvm;

import eu.pmsoft.sam.see.api.model.ExecutionStrategy;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.model.SamInstanceTransaction;
import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.sam.see.api.transaction.SamInjectionModelVisitor;


public class SamInjectionTransactionObject implements SamInstanceTransaction {

    private final SamInjectionConfiguration configuration;
    private final SIURL transactionURL;
    private final ExecutionStrategy executionStrategy;


    public SamInjectionTransactionObject(SamInjectionConfiguration configuration, SIURL transactionURL, ExecutionStrategy executionStrategy) {
        this.configuration = configuration;
        this.transactionURL = transactionURL;
        this.executionStrategy = executionStrategy;
    }

    @Override
    public ExecutionStrategy getExecutionStrategy() {
        return executionStrategy;
    }

    @Override
    public SIURL getTransactionURL() {
        return transactionURL;
    }

    @Override
    public SamInjectionConfiguration getInjectionConfiguration() {
        return configuration;
    }

    public <T> T accept(SamInjectionModelVisitor<T> visitor) {
        return visitor.visitTransaction(this);
    }

}