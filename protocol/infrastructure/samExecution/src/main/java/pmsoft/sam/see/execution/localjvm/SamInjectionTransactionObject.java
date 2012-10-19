package pmsoft.sam.see.execution.localjvm;

import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInstanceTransaction;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import pmsoft.sam.see.api.transaction.SamInjectionModelVisitor;


public class SamInjectionTransactionObject implements SamInstanceTransaction {

	private final SamInjectionConfiguration configuration;
	private final SIURL transactionURL;


	public SamInjectionTransactionObject(SamInjectionConfiguration configuration, SIURL transactionURL) {
		this.configuration = configuration;
		this.transactionURL = transactionURL;
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
		visitor.enterTransaction(this);
		configuration.accept(visitor);
		return visitor.exitTransaction(this);
		
	}

}