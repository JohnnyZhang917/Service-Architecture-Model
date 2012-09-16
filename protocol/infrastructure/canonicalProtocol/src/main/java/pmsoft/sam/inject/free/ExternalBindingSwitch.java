package pmsoft.sam.inject.free;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;

public class ExternalBindingSwitch<T> implements InvocationHandler {

	private final Key<T> key;
	private final long instanceReferenceNr;
	private final int serviceSlotNr;

	@Inject
	private Provider<ExternalInstanceProvider> providerExternalInstanceProvider;

	@SuppressWarnings("unchecked")
	public ExternalBindingSwitch(Key<T> key, long instanceReferenceNr, int slotNr) {
		Class<? super T> mainClass = key.getTypeLiteral().getRawType();
		recordReference = (T) Proxy.newProxyInstance(mainClass.getClassLoader(), new Class[] { mainClass }, this);
		this.instanceReferenceNr = instanceReferenceNr;
		this.key = key;
		this.serviceSlotNr = slotNr;
	}

	private T recordReference;

	public T getReferenceObject() {
		return recordReference;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		T internalReference = getInternalInstance();
		return method.invoke(internalReference , args);
	}
	
	public T getInternalInstance(){
		ExternalInstanceProvider externalProvider = providerExternalInstanceProvider.get();
		T internalReference = externalProvider.getReference(key, instanceReferenceNr,serviceSlotNr);
		return internalReference;
	}
}
