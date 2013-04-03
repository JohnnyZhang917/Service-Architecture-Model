package eu.pmsoft.exceptions;

import com.google.common.base.Objects;

/**
 * Catch on the head of a operation context to look for internal errors.
 * <p/>
 * Normally You will translate this to a OperationCheckedException and throw it to the client code.
 * The client should try to fix or repeat the operation.
 */
public class OperationRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -6664261440597260760L;
    private final ErrorsReport errors;

    public OperationRuntimeException(ErrorsReport errorsReport) {
        this.errors = errorsReport;
    }

    public OperationRuntimeException(String message) {
        super(message);
        this.errors = null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("errors", errors)
                .toString();
    }
}
