package pmsoft.sam.inject.wrapper;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import pmsoft.sam.canonical.api.CanonicalProtocolRequestHandler;
import pmsoft.sam.canonical.api.recording.RecordingContextImpl;
import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;
import pmsoft.sam.inject.free.ExternalBindingController;
import pmsoft.sam.inject.free.ExternalInstanceProvider;

import com.google.common.collect.Maps;
import com.google.inject.Key;

public class WrappingGlobalController implements WrappingInjectorControllerInternal {

	private final Map<ExternalBindingController, ExternalInstanceProvider> serviceControlContext = Maps.newHashMap();

	// FIXME interface hierarchy, put a interface and not a class here: 
	private final RecordingContextImpl canonicalProtocolProvider;

	public WrappingGlobalController(CanonicalProtocolExecutionService canonicalExecutionService, URL transactionURL) {
		canonicalProtocolProvider = new RecordingContextImpl(canonicalExecutionService, UUID.randomUUID(), transactionURL);
	}

	public CanonicalProtocolRequestHandler getRequestHandler() {
		return canonicalProtocolProvider;
	}
	
	private boolean bound = false;

	public boolean bindExecutionContext() {
		if (bound) {
			return false;
		}
		Set<Entry<ExternalBindingController, ExternalInstanceProvider>> controlEntries = serviceControlContext.entrySet();
		for (Entry<ExternalBindingController, ExternalInstanceProvider> entry : controlEntries) {
			entry.getKey().bindRecordContext(entry.getValue());
		}
		bound = true;
		return true;
	}

	public void unbindExecutionContext() {
		//FIXME is it necessary to save the stack of execution contexts?????  make a test to check this
		Set<ExternalBindingController> serviceControl = serviceControlContext.keySet();
		for (ExternalBindingController externalBindingController : serviceControl) {
			externalBindingController.unBindRecordContext();
		}
		bound = false;
	}

	public void registerServiceInstanceController(ExternalBindingController internalServiceControl,
			ExternalInstanceProvider wrapperConfiguration) {
		checkNotNull(internalServiceControl);
		checkNotNull(wrapperConfiguration);
		serviceControlContext.put(internalServiceControl, wrapperConfiguration);
	}

	public InstanceProvider getExternalInstanceProvider(URL externalReference, Collection<Key<?>> allowedBindings) {
		// FIXME the canonical protocol don't check that provided keys are
		// allowed by the service specification
		// The use of the definition API disallow the pass of bad Key bindings
		// (is this true???) so no check is implemented
		return canonicalProtocolProvider.registerExternalInstanceProvider(externalReference);
	}

}
