package eu.pmsoft.sam.see.execution.localjvm;

import eu.pmsoft.see.api.model.STID;
import eu.pmsoft.see.api.model.SamServiceInstanceTransaction;
import eu.pmsoft.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.see.api.transaction.SamInjectionModelVisitor;


public class SamServiceInjectionTransactionObject implements SamServiceInstanceTransaction {

    private final SamInjectionConfiguration configuration;
    private final STID transactionURL;

    public SamServiceInjectionTransactionObject(SamInjectionConfiguration configuration, STID transactionURL) {
        this.configuration = configuration;
        this.transactionURL = transactionURL;
    }

    @Override
    public STID getTransactionURL() {
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
