package eu.pmsoft.sam.injection;


import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

class ExternalBindingScope implements Scope, ExternalBindingController {

    private final ThreadLocal<ExternalInstanceProvider> perThreadServiceExecutionRecorder;

    ExternalBindingScope() {
        perThreadServiceExecutionRecorder = new ThreadLocal<ExternalInstanceProvider>();
    }

    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return new Provider<T>() {
            @SuppressWarnings("unchecked")
            public T get() {
                ExternalInstanceProvider value = perThreadServiceExecutionRecorder.get();
                assert value != null;
                return (T) value;
            }
        };
    }

    @Override
    public void bindRecordContext(DependenciesBindingContext context) {
        perThreadServiceExecutionRecorder.set(context.getExternalInstanceProvider());
    }

    @Override
    public void unBindRecordContext() {
        perThreadServiceExecutionRecorder.remove();
    }

}
