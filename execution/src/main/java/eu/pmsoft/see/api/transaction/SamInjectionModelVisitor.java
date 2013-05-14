package eu.pmsoft.see.api.transaction;

import eu.pmsoft.see.api.model.SamServiceInstanceTransaction;


public interface SamInjectionModelVisitor<T> {

    public T visitTransaction(SamServiceInstanceTransaction transaction);

    public void visit(SamInjectionConfiguration configuration);

    public void visit(BindPointSIID bindPointSIID);

    public void visit(BindPointSIURL bindPointSIURL);

    public void visit(BindPointNestedTransaction bindPointTransaction);

}
