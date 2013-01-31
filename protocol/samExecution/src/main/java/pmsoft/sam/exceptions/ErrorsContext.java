package pmsoft.sam.exceptions;

public class ErrorsContext {


    public void addError(Throwable e, String format, Object... objects) {


    }

    public void addError(Throwable others) {


    }

    public boolean hasErrors() {
        return false;

    }

    public SamException toException() {
        return new SamException(this);  //To change body of created methods use File | Settings | File Templates.
    }
}
