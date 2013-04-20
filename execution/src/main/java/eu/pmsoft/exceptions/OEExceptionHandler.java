package eu.pmsoft.exceptions;

public interface OEExceptionHandler<X,R> {

    public R handleException(X exceptionChain);
}
