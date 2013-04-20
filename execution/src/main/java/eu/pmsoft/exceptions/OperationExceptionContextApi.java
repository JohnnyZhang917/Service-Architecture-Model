package eu.pmsoft.exceptions;

public interface OperationExceptionContextApi<U extends RuntimeException> {

    OEContext<U> openNestedOperationContext();

    OEContextApi getCurrentOperationContext();

}
