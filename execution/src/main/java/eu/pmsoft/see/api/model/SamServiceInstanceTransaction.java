package eu.pmsoft.see.api.model;

import eu.pmsoft.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.see.api.transaction.SamInjectionModelVisitor;

public interface SamServiceInstanceTransaction {

    public STID getTransactionURL();

    public SamInjectionConfiguration getInjectionConfiguration();

    public <T> T accept(SamInjectionModelVisitor<T> visitor);

}
