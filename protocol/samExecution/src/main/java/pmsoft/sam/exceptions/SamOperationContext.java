package pmsoft.sam.exceptions;

public class SamOperationContext {

    private final ErrorsContext errors;

    public SamOperationContext() {
        errors = new ErrorsContext();
    }

    public void throwOnErrors() throws SamException {
        if( errors.hasErrors()){
            throw errors.toException();
        }
    }

    public ErrorsContext getErrors() {
        return errors;
    }
}
