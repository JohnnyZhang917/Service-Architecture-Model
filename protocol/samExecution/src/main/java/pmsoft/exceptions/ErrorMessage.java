package pmsoft.exceptions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

public class ErrorMessage {
    private final Object[] objects;
    private final Throwable exception;
    private final String format;

    public ErrorMessage(Throwable e, String format, Object[] objects) {
        this.exception = e;
        this.format = format;
        this.objects = objects;
    }

    public ErrorMessage(String format, Object[] objects) {
        this(null,format,objects);
    }

    public ErrorMessage(Throwable exception) {
        this(exception,null,null);
    }

    @Override
    public String toString() {
        // FIXME report string
        if( format!= null ){
            return format(format,objects);
        }
        if( exception != null) {
            return exception.getMessage();
        }
        return Objects.toStringHelper(this).omitNullValues()
                .add("objects", objects)
                .add("exception", exception)
                .add("format", format)
                .toString();
    }

    /**
     * copy from Preconditions of guava
     */
    static String format(String template,
                         @Nullable Object[] args) {
        template = String.valueOf(template); // null -> "null"

        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(
                template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }
}
