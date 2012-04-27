package pmsoft.sam.inject.wrapper;

import java.net.URL;
import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Represents a binding configuration mapping target.
 * 
 * When realServiceInjector is not null, then the mapping provide a final
 * service implementation.
 * 
 * When realServiceInjector is null, then the mapping is not defined and a
 * recording instance should be binded.
 * 
 * @author Paweł Cesar Sanjuan Szklarz
 * 
 */
public class ServiceBindingDefinition {

	private final ImmutableSet<Key<?>> serviceKeys;
	private final Injector realServiceInjector;
	private final URL externalInstanceURL;

	public ServiceBindingDefinition(Collection<Key<?>> serviceKeys, Injector realServiceInjector) {
		this.serviceKeys = ImmutableSet.copyOf(serviceKeys);
		this.realServiceInjector = realServiceInjector;
		this.externalInstanceURL = null;
	}

	public ServiceBindingDefinition(Collection<Key<?>> serviceKeys, URL externalInstanceURL) {
		this.serviceKeys = ImmutableSet.copyOf(serviceKeys);
		this.realServiceInjector = null;
		this.externalInstanceURL = externalInstanceURL;
	}

	public Collection<Key<?>> getServiceKeys() {
		return serviceKeys;
	}

	public Injector getRealServiceInjector() {
		return realServiceInjector;
	}

	public URL getExternalInstanceURL() {
		return externalInstanceURL;
	}


}
