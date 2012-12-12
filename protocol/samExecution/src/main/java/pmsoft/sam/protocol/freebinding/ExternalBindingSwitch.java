package pmsoft.sam.protocol.freebinding;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * Provide switching capabilities of injected references.
 * 
 * The direct reference to service contract interfaces refer to a Proxy with
 * handler type ExternalBindingSwitch. The fields
 * (key,instanceReferenceNr,serviceSlotNr) uniquely identify a injected object.
 * 
 * The mapping
 * " (key,instanceReferenceNr,serviceSlotNr) -> real implementation " is
 * provided by a ExternalInstanceProvider. 
 * 
 * ExternalInstanceProvider is switched on runtime, according to the current InjectionConfiguration and Transaction.
 * 
 * @see pmsoft.sam.protocol.freebinding.ExternalBindingController
 * @author pawel
 * 
 * @param <T>
 */
public class ExternalBindingSwitch<T> implements InvocationHandler {

	private final Key<T> key;
	private final long instanceReferenceNr;
	private final int serviceSlotNr;

	@Inject
	private Provider<ExternalInstanceProvider> providerExternalInstanceProvider;

	@SuppressWarnings("unchecked")
	public ExternalBindingSwitch(Key<T> key, long instanceReferenceNr, int slotNr) {
		Class<? super T> mainClass = key.getTypeLiteral().getRawType();
		recordReference = (T) Proxy.newProxyInstance(mainClass.getClassLoader(), new Class<?>[] { mainClass }, this);
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
		return method.invoke(internalReference, args);
	}

	public T getInternalInstance() {
		ExternalInstanceProvider externalProvider = providerExternalInstanceProvider.get();
		T internalReference = externalProvider.getReference(key, instanceReferenceNr, serviceSlotNr);
		return internalReference;
	}
}
