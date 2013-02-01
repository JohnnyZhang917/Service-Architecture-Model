package pmsoft.exceptions;

public class ErrorsReport {


    public void addError(Throwable e, String format, Object... objects) {


    }

    public void addError(Throwable others) {


    }

    public boolean hasErrors() {
        return false;

    }

    public OperationCheckedException toException() {
        return new OperationCheckedException(this);  //To change body of created methods use File | Settings | File Templates.
    }
}
