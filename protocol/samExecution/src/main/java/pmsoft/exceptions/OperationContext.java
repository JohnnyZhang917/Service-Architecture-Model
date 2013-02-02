package pmsoft.exceptions;

public class OperationContext {

    private final ErrorsReport errors;

    private final OperationContext parent;

    public OperationContext(OperationContext parent) {
        this.parent = parent;
        errors = new ErrorsReport();
    }

    public OperationContext getParent() {
        return parent;
    }

    public void throwOnErrors() throws OperationCheckedException {
        if( errors.hasErrors()){
            throw errors.toException();
        }
    }

    public OperationRuntimeException getRuntimeError(){
        return errors.toRuntimeException();
    }

    public ErrorsReport getErrors() {
        return errors;
    }
}
