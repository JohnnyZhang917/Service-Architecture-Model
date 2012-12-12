package pmsoft.execution;

import com.google.inject.Key;

import java.net.URL;

public abstract class ServiceAction<R, T> {

	private final URL serviceURL;
	private final Key<T> interfaceKey;

	public ServiceAction(URL serviceURL, Key<T> interfaceKey) {
		this.serviceURL = serviceURL;
		this.interfaceKey = interfaceKey;
	}

	public abstract R executeInteraction(T service);
	
	public URL getServiceURL() {
		return serviceURL;
	};
	
	public Key<T> getInterfaceKey() {
		return interfaceKey;
	}
}
