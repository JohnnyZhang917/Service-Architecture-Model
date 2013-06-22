package eu.pmsoft.sam.execution;

import com.google.inject.Key;

import java.net.URL;

public abstract class ServiceAction<R, T> {

    private final Key<T> interfaceKey;

    public ServiceAction( Key<T> interfaceKey) {
        this.interfaceKey = interfaceKey;
    }

    public abstract R executeInteraction(T service);

    public Key<T> getInterfaceKey() {
        return interfaceKey;
    }
}
