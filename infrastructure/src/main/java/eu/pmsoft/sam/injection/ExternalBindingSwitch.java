package eu.pmsoft.sam.injection;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ExternalBindingSwitch<T> implements InvocationHandler {

    private final Key<T> key;
    private final int instanceReferenceNr;
    private final int slotNr;

    @Inject
    private Provider<ExternalInstanceProvider> providerExternalInstanceProvider;

    @SuppressWarnings("unchecked")
    public ExternalBindingSwitch(Key<T> key, int instanceReferenceNr, int slotNr) {
        Class<? super T> mainClass = key.getTypeLiteral().getRawType();
        this.recordReference = (T) Proxy.newProxyInstance(mainClass.getClassLoader(), new Class<?>[]{mainClass}, this);
        this.instanceReferenceNr = instanceReferenceNr;
        this.slotNr = slotNr;
        this.key = key;
    }

    private T recordReference;

    public T getReferenceObject() {
        return recordReference;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        T internalReference = getInternalInstance();
        return method.invoke(internalReference, args);
    }

    public T getInternalInstance() {
        ExternalInstanceProvider externalProvider = providerExternalInstanceProvider.get();
        T internalReference = externalProvider.getReference(slotNr,key, instanceReferenceNr);
        return internalReference;
    }
}
