package deprecated.pmsoft.sam.module.see.transaction;


public interface TransactionConfigurator {

	public InjectionConfiguration getInjectionConfiguration();
	
	public SamTransaction createTransactionContext();

}
