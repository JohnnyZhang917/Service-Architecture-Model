package pmsoft.sam.see.api.transaction;

import pmsoft.sam.see.api.model.SamInstanceTransaction;


public interface SamInjectionModelVisitor<T> {
	
//	public void enterExecution(SamInstanceTransaction transaction);
//	
//	public T exitExecution(SamInstanceTransaction transaction);
	
	public T visitTransaction(SamInstanceTransaction transaction);

//	public void enterNested(SamInjectionConfiguration samInjectionTransactionConfiguration);
//	
//	public void exitNested(SamInjectionConfiguration samInjectionTransactionConfiguration);
	
	public void visit(SamInjectionConfiguration configuration);

	public void visit(BindPointSIID bindPointSIID);

	public void visit(BindPointSIURL bindPointSIURL);

	public void visit(BindPointNestedTransaction bindPointTransaction);
	
}
