package pmsoft.sam.exceptions;

public class SamException extends Exception{
    private static final long serialVersionUID = -4595491699954737592L;
    private final ErrorsContext errors;

    public SamException(ErrorsContext errorsContext) {
        this.errors = errorsContext;
    }
}
