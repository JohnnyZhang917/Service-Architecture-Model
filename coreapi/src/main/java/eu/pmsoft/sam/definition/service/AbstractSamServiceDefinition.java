package eu.pmsoft.sam.definition.service;

import com.google.inject.Key;

import java.lang.annotation.Annotation;

/**
 * Definition of ONE service contract ( a set of keys )
 *
 * @author pawel
 */
public abstract class AbstractSamServiceDefinition implements SamServiceDefinition {

    private SamServiceDefinitionLoader.SamServiceDefinitionInterfacesLoader loaderRef;

    public void loadServiceDefinition(SamServiceDefinitionLoader loader) {
        try {
            this.loaderRef = loader.definedIn(this.getClass());
            loadServiceDefinition();
        } finally {
            this.loaderRef = null;
        }
    }

    abstract public void loadServiceDefinition();

    protected void withKey(Class<?> interfaceReference) {
        loaderRef.withKey(interfaceReference);
    }

    protected void withKey(Class<?> interfaceReference, Annotation annotation) {
        loaderRef.withKey(interfaceReference, annotation);
    }

    protected void withKey(Key<?> key) {
        loaderRef.withKey(key);
    }

}
