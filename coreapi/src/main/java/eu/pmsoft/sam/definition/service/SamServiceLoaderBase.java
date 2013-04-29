package eu.pmsoft.sam.definition.service;

import java.lang.annotation.Annotation;

public interface SamServiceLoaderBase {
    // regular grammar:
    // Service: definitionClass Key*
    // Key : interface | interface annotation

    SamServiceLoader setupLoadContext(Class<? extends SamServiceDefinition> definitionClass);

    public interface SamServiceLoader {

        void addInterface(Class<?> interfaceReference);

        void addInterface(Class<?> interfaceReference, Annotation annotation);

    }

}
