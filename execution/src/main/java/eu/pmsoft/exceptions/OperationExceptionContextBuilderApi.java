package eu.pmsoft.exceptions;

public interface OperationExceptionContextBuilderApi {
    public OperationExceptionContextApi exceptionContext(Class<? extends RuntimeException> rootRuntime);
}
