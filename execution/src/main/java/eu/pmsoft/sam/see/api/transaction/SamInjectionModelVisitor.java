package eu.pmsoft.sam.see.api.transaction;

import eu.pmsoft.sam.see.api.model.SamServiceInstanceTransaction;


public interface SamInjectionModelVisitor<T> {

    public T visitTransaction(SamServiceInstanceTransaction transaction);

    public void visit(SamInjectionConfiguration configuration);

    public void visit(BindPointSIID bindPointSIID);

    public void visit(BindPointSIURL bindPointSIURL);

    public void visit(BindPointNestedTransaction bindPointTransaction);

}
