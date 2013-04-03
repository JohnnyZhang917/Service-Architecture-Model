package eu.pmsoft.sam.protocol.transport.data;

import com.google.inject.Key;
import eu.pmsoft.sam.protocol.transport.BindingKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class SerializableKey<T> {

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

    public static <T> BindingKey fromKeySerializable(Key<T> key) {
        BindingKey bkey = new BindingKey();
        bkey.setType(key.getTypeLiteral().getRawType().getCanonicalName());
        if (key.getAnnotationType() != null) {
            bkey.setAnnotationType(key.getAnnotationType().getCanonicalName());
        }
        //TODO annotation instance
        return bkey;  //To change body of created methods use File | Settings | File Templates.
    }

    public static Key<?> toKey(BindingKey bindingKey) {
        try {
            if (bindingKey.getAnnotationType() == null) {
                return Key.get(Class.forName(bindingKey.getType()));
            } else {
                return Key.get(Class.forName(bindingKey.getType()), (Class<? extends Annotation>) Class.forName(bindingKey.getAnnotationType()));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("TODO", e);
        }
    }
}
