package pmsoft.sam.definition.service;

import java.lang.annotation.Annotation;

/**
 * Definition of ONE service contract ( a set of interfaces )
 *
 * @author pawel
 */
public abstract class AbstractSamServiceDefinition implements
        SamServiceDefinition {

    private SamServiceLoader loaderRef;

    public void loadServiceDefinition(SamServiceLoader loader) {
        try {
            loader.setupLoadContext(this.getClass());
            this.loaderRef = loader;
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
