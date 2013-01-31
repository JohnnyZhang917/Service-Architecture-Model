package pmsoft.sam.definition.service;

import java.lang.annotation.Annotation;

public interface SamServiceLoader {
	// regular grammar:
	// Service: definitionClass Key*
    // Key : interface | interface annotation

	void addInterface(Class<?> interfaceReference);
	
	void addInterface(Class<?> interfaceReference, Annotation annotation);

	void setupLoadContext(Class<? extends SamServiceDefinition> definitionClass);

}
