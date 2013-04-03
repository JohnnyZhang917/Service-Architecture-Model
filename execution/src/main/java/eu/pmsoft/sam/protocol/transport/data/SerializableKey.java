package eu.pmsoft.sam.protocol.transport.data;

import com.google.inject.Key;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class SerializableKey<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8700737004191999401L;
    private final Type type;
    private final Class<? extends Annotation> annotationType;
    private final Annotation annotationInstance;

    public static <T> SerializableKey<T> fromKey(Key<T> guiceKey) {
        return new SerializableKey<T>(guiceKey);
    }

    public SerializableKey(Key<T> key) {
        this.type = key.getTypeLiteral().getType();
        this.annotationType = key.getAnnotationType();
        this.annotationInstance = key.getAnnotation();
    }

    public Key<?> getKey() {
        if (annotationInstance != null) {
            return Key.get(type, annotationInstance);
        } else {
            return annotationType != null ? Key.get(type, annotationType) : Key.get(type);
        }

    }

    @Override
    public String toString() {
        return "SerializableKey [type=" + type + ", annotationType=" + annotationType + ", annotationInstance=" + annotationInstance + "]";
    }

}
