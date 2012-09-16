package pmsoft.sam.inject.wrapper;

import java.net.URL;
import java.util.Collection;

import pmsoft.sam.inject.free.ExternalBindingController;
import pmsoft.sam.inject.free.ExternalInstanceProvider;

import com.google.inject.Key;

public interface WrappingInjectorControllerInternal extends WrappingInjectorController {

	void registerServiceInstanceController(ExternalBindingController internalServiceControl,
			ExternalInstanceProvider wrapperConfiguration);
	
	InstanceProvider getExternalInstanceProvider(URL externalReference, Collection<Key<?>> allowedBindings);

}
