package pmsoft.sam.protocol.execution.model;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.inject.Key;

public class SerializableKey<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8700737004191999401L;
	private final Type type;
	private final Class<? extends Annotation> annotationType;
	private final Annotation annotationInstance;

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

}
