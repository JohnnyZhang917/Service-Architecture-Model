package pmsoft.sam.see.api.transaction;

import pmsoft.sam.see.api.model.SamInstanceTransaction;


public interface SamInjectionModelVisitor<T> {
	
	public void enterTransaction(SamInstanceTransaction transaction);
	
	public T exitTransaction(SamInstanceTransaction transaction);

	public void enterNested(SamInjectionConfiguration samInjectionTransactionConfiguration);
	
	public void exitNested(SamInjectionConfiguration samInjectionTransactionConfiguration);

	public void visit(BindPointSIID bindPointSIID);

	public void visit(BindPointSIURL bindPointSIURL);

	public void visit(BindPointTransaction bindPointTransaction);
	
}
