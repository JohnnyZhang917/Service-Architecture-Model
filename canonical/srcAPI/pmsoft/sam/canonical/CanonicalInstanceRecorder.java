package pmsoft.sam.canonical;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import pmsoft.sam.canonical.api.ServiceSlotRecordingContext;

import com.google.inject.Key;

public class CanonicalInstanceRecorder<T> implements InvocationHandler {

	private final ServiceSlotRecordingContext context;
	private final T instance;
	private final Key<T> key;
	private final int serviceInstanceNr;
	private final int serviceSlotNr;

	@SuppressWarnings("unchecked")
	public CanonicalInstanceRecorder(ServiceSlotRecordingContext context, Key<T> key, int serviceInstanceNr, int serviceSlotNr) {
		super();
		this.context = context;
		this.key = key;
		this.serviceInstanceNr = serviceInstanceNr;
		this.serviceSlotNr = serviceSlotNr;
		Class<T> returnedType = (Class<T>) key.getTypeLiteral().getRawType();
		instance = (T) Proxy.newProxyInstance(returnedType.getClassLoader(), new Class[] { returnedType }, this);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> returnType = method.getReturnType();
		if (returnType.equals(Void.TYPE)) {
			context.recordExternalMethodCall(key, serviceInstanceNr, method, args);
			return null;
		}
		if (returnType.isInterface()) {
			CanonicalInstanceRecorder<?> handler = context.recordExternalMethodCallWithInterfaceReturnType(key, serviceInstanceNr,
					method, args, returnType);
			return handler.getInstance();
		}
		return context.recordExternalMethodCallWithReturnType(key, serviceInstanceNr, method, args, returnType);

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

}
