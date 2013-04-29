package eu.pmsoft.execution;

import com.google.inject.Key;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.model.STID;

import java.net.URL;

public abstract class ServiceAction<R, T> {

    private final STID serviceURL;
    private final Key<T> interfaceKey;

    public ServiceAction(STID serviceURL, Key<T> interfaceKey) {
        this.serviceURL = serviceURL;
        this.interfaceKey = interfaceKey;
    }

    public abstract R executeInteraction(T service);

    public STID getServiceURL() {
        return serviceURL;
    }

    public Key<T> getInterfaceKey() {
        return interfaceKey;
    }
}
