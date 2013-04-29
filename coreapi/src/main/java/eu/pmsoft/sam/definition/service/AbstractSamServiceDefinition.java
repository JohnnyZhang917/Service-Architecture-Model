package eu.pmsoft.sam.definition.service;

import java.lang.annotation.Annotation;

/**
 * Definition of ONE service contract ( a set of interfaces )
 *
 * @author pawel
 */
public abstract class AbstractSamServiceDefinition implements
        SamServiceDefinition {

    private SamServiceLoaderBase.SamServiceLoader loaderRef;

    public void loadServiceDefinition(SamServiceLoaderBase loader) {
        try {

            this.loaderRef = loader.setupLoadContext(this.getClass());;
            loadServiceDefinition();
        } finally {
            this.loaderRef = null;
        }
    }

    abstract public void loadServiceDefinition();

    protected void addInterface(Class<?> interfaceReference) {
        loaderRef.addInterface(interfaceReference);
    }

    protected void addInterface(Class<?> interfaceReference, Annotation annotation) {
        loaderRef.addInterface(interfaceReference, annotation);
    }
}
