package eu.pmsoft.sam.definition.service;

import com.google.inject.Key;

import java.lang.annotation.Annotation;

public interface SamServiceDefinitionLoader {

    SamServiceDefinitionInterfacesLoader definedIn(Class<? extends SamServiceDefinition> definitionClass);

    public interface SamServiceDefinitionInterfacesLoader {

        void withKey(Class<?> interfaceReference);

        void withKey(Class<?> interfaceReference, Annotation annotation);

        void withKey(Key<?> key);

    }

}
