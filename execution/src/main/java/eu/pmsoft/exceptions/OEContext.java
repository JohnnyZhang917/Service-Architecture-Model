package eu.pmsoft.exceptions;


public interface OEContext<U extends RuntimeException> {

    void close();

    void chainException(Exception exception);

    void mayThrow(Class<? extends Exception> exceptionClass);

    void catchException(U operationException);
}
