package pmsoft.sam.see.api.transaction;


public class BindPointNestedTransaction extends BindPoint {

	final SamInjectionConfiguration configuration;

	public BindPointNestedTransaction(SamInjectionConfiguration transaction) {
		super(transaction.getProvidedService());
		this.configuration = transaction;
	}

	@Override
	public <T> void accept(SamInjectionModelVisitor<T> visitor) {
		visitor.visit(this);
	}
	
}
