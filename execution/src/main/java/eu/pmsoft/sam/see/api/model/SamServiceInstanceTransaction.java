package eu.pmsoft.sam.see.api.model;

import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.sam.see.api.transaction.SamInjectionModelVisitor;

public interface SamServiceInstanceTransaction {

    public STID getTransactionURL();

    public SamInjectionConfiguration getInjectionConfiguration();

    public <T> T accept(SamInjectionModelVisitor<T> visitor);

}
