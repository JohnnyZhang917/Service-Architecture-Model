package pmsoft.sam.inject.wrapper.deprecated;

import java.net.URL;
import java.util.Collection;

import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;
import pmsoft.sam.protocol.injection.InstanceProvider;

import com.google.inject.Key;

public interface WrappingInjectorControllerInternal extends WrappingInjectorController {

	void registerServiceInstanceController(ExternalBindingController internalServiceControl,
			ExternalInstanceProvider wrapperConfiguration);
	
	InstanceProvider getExternalInstanceProvider(URL externalReference, Collection<Key<?>> allowedBindings);

}
