package pmsoft.exceptions;

/**
 * The executed operation has failed. The client should try to fix or repeat the operations.
 */
public class OperationCheckedException extends Exception {
    private static final long serialVersionUID = -4595491699954737592L;
    private final ErrorsReport errors;

    public OperationCheckedException(ErrorsReport errorsReport) {
        this.errors = errorsReport;
    }
}
