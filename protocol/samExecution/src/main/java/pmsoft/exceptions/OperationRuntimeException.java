package pmsoft.exceptions;

import com.google.common.base.Objects;

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
