package eu.pmsoft.exceptions;

import com.google.common.base.Objects;

public class ErrorMessage {

    private final String message;
    private final Throwable cause;

    public ErrorMessage(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("message", message)
                .add("cause", cause)
                .toString();
    }
}
