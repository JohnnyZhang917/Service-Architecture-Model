package pmsoft.exceptions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;

public class ErrorsReport {

    List<ErrorMessage> errors = Lists.newArrayList();

    public void addError(Throwable e, String format, Object... objects) {
        errors.add(new ErrorMessage(e,format,objects));
    }

    public void addError( String format, Object... objects) {
        errors.add(new ErrorMessage(format,objects));
    }

    public void addError(Throwable exception) {
        errors.add(new ErrorMessage(exception));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public OperationCheckedException toException() {
        return new OperationCheckedException(this);
    }

    public OperationRuntimeException toRuntimeException(){
        return new OperationRuntimeException(this);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("errors", errors)
                .toString();
    }
}
