package eu.pmsoft.sam.protocol.record;

import com.google.common.base.Objects;
import com.google.inject.Key;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class CanonicalInstanceRecorder<T> implements InvocationHandler {

    private final MethodRecordContext executionContext;
    private final T instance;
    private final Key<T> key;
    private final int serviceInstanceNr;
    private final int serviceSlotNr;

    @SuppressWarnings("unchecked")
    public CanonicalInstanceRecorder(MethodRecordContext executionContext, Key<T> key, int serviceInstanceNr, int serviceSlotNr) {
        super();
        this.executionContext = executionContext;
        this.key = key;
        this.serviceInstanceNr = serviceInstanceNr;
        this.serviceSlotNr = serviceSlotNr;
        Class<T> returnedType = (Class<T>) key.getTypeLiteral().getRawType();
        instance = (T) Proxy.newProxyInstance(returnedType.getClassLoader(), new Class<?>[]{returnedType}, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        if (method.getName().equals("toString")) {
            return this.toString();
        }
        if (returnType.equals(Void.TYPE)) {
            executionContext.recordExternalMethodCall(serviceSlotNr, serviceInstanceNr, method, args);
            return null;
        }
        if (returnType.isInterface()) {
            CanonicalInstanceRecorder<?> handler = executionContext.recordExternalMethodCallWithInterfaceReturnType(serviceSlotNr, serviceInstanceNr,
                    method, args, returnType);
            return handler.getInstance();
        }
        return executionContext.recordExternalMethodCallWithReturnType(serviceSlotNr, serviceInstanceNr, method, args, returnType);

    }

    public T getInstance() {
        return instance;
    }

    public int getServiceInstanceNr() {
        return serviceInstanceNr;
    }

    public int getServiceSlotNr() {
        return serviceSlotNr;
    }

    public Key<T> getKey() {
        return key;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("key", key)
                .add("serviceInstanceNr", serviceInstanceNr)
                .add("serviceSlotNr", serviceSlotNr)
                .toString();
    }
}
