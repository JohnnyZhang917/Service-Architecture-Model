package pmsoft.sam.see;

import pmsoft.sam.see.api.model.SIURL;

import com.google.inject.Key;

public abstract class ServiceInteracion<T> {

	final SIURL localServiceURL;
	final Key<T> interfaceKey;

	public ServiceInteracion(SIURL localServiceURL, Key<T> interfaceKey) {
		this.localServiceURL = localServiceURL;
		this.interfaceKey = interfaceKey;
	}

	public abstract void executeInteraction(T service);
}
