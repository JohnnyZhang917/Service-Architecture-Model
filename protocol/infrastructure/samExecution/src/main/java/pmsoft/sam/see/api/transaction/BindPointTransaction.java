package pmsoft.sam.see.api.transaction;


public class BindPointTransaction extends BindPoint {

	final SamInjectionConfiguration transaction;

	public BindPointTransaction(SamInjectionConfiguration transaction) {
		super(transaction.getProvidedService());
		this.transaction = transaction;
	}

	@Override
	public <T> void accept(SamInjectionModelVisitor<T> visitor) {
		visitor.visit(this);
		transaction.accept(visitor);
	}
	
}
