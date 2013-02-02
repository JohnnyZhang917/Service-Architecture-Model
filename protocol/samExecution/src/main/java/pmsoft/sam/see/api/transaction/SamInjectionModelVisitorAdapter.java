package pmsoft.sam.see.api.transaction;

import com.google.common.collect.ImmutableList;
import pmsoft.sam.see.api.model.SamInstanceTransaction;

public class SamInjectionModelVisitorAdapter<T> implements SamInjectionModelVisitor<T> {

	protected void visitNested(SamInjectionConfiguration configuration) {
		ImmutableList<BindPoint> bindPoints = configuration.getBindPoints();
		for (BindPoint bindPoint : bindPoints) {
			bindPoint.accept(this);
		}
	}
	
	@Override
	public T visitTransaction(SamInstanceTransaction transaction) {
		transaction.getInjectionConfiguration().accept(this);
		return null;
	}

	@Override
	public void visit(SamInjectionConfiguration configuration) {
		visitNested(configuration);
	}

	@Override
	public void visit(BindPointSIID bindPointSIID) {
		
	}

	@Override
	public void visit(BindPointSIURL bindPointSIURL) {
		
	}

	@Override
	public void visit(BindPointNestedTransaction bindPointTransaction) {
		visit(bindPointTransaction.configuration);
	}

}
