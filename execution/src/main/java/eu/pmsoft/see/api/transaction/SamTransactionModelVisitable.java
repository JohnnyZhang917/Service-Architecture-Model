package eu.pmsoft.see.api.transaction;

public interface SamTransactionModelVisitable {

    public <T> void accept(SamInjectionModelVisitor<T> visitor);

}
