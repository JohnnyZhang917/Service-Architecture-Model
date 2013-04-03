package pmsoft.exceptions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;

public class ErrorsReport {
    private List<ErrorMessage> errors = Lists.newArrayList();

    public void addError(Throwable cause, String format, Object... objects) {
        String message = ErrorsReport.format(format, objects);
        errors.add(new ErrorMessage(message,cause));
    }

    public void addError(String format, Object... objects) {
        String message = ErrorsReport.format(format, objects);
        errors.add(new ErrorMessage(message, null));
    }

    public void addError(Throwable cause) {
        errors.add(new ErrorMessage(null,cause));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public OperationCheckedException toCheckedException() {
        return new OperationCheckedException(this);
    }

    public OperationRuntimeException toRuntimeException() {
        return new OperationRuntimeException(this);
    }

    private static String format(String messageFormat, Object[] arguments) {
        return String.format(messageFormat, arguments);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("errors", errors)
                .toString();
    }
}
