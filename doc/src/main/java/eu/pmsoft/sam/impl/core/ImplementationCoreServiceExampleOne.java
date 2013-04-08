package eu.pmsoft.sam.impl.core;

import eu.pmsoft.sam.binding.BindingAnnotationExample;
import eu.pmsoft.sam.service.core.CoreServiceExample;

import javax.inject.Inject;

public class ImplementationCoreServiceExampleOne implements CoreServiceExample {

    @Inject
    private CoreServiceExample serviceInjectionPoint;

    @Inject
    @BindingAnnotationExample
    private CoreServiceExample serviceInjectionPointWithBindingAnnotation;

    private Integer counter;
    private Integer limit = 100;

    public void resetProcess() {
        counter = 0;
    }

    public void putData(int value) {
        counter += value;
    }

    public boolean isProcessStatusOk() {
        return counter < limit;
    }


}
