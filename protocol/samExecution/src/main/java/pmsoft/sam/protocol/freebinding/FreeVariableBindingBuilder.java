package pmsoft.sam.protocol.freebinding;

import java.util.List;


import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class FreeVariableBindingBuilder {

	/**
	 * Create a module with providers of all passed Keys.
	 * The providers will refer to a ExternalBindingSwitch that will look for the real instance at runtime by meaning of the Canonical Protocol.
	 * 
	 * At runtime, instance switching is make be the ExternalBindingSwitch instance returned by the providers.
	 * 
	 * The ExtrenalBindingInfrastructureModule provide a special scope and a controller to setup on thread local ExternalInstanceProvider.
	 * 
	 * @param injectedFreeVariableBinding
	 * @return module with key to provided configuration
	 */
	public final static Module createFreeBindingModule(final List<List<Key<?>>> injectedFreeVariableBinding) {
		return new PrivateModule() {

			private final ImmutableList<List<Key<?>>> serviceKeys = ImmutableList.copyOf(injectedFreeVariableBinding);

			@Override
			protected void configure() {
				binder().requireExplicitBindings();
				int slotNr = 0;
				for (List<Key<?>> currentServiceKeys : serviceKeys) {
					for (Key<?> key : currentServiceKeys) {
						createBinding(key,slotNr);
					}
					slotNr++;
				}
				install(new ExtrenalBindingInfrastructureModule());
				expose(ExternalBindingController.class);
			}

			private <T> void createBinding( Key<T> key, int slotNr) {
				Provider<T> recorder = createIntermediumProvider(key,slotNr);
				bind(key).toProvider(recorder);
				expose(key);
			}

			private <T> Provider<T> createIntermediumProvider( final Key<T> key, final int slotNr) {
				return new Provider<T>() {
					@Inject
					private Injector injector;
					private volatile long instanceNr = 0;

					public T get() {
						ExternalBindingSwitch<T> intermedium = new ExternalBindingSwitch<T>(key, instanceNr++,slotNr);
						injector.injectMembers(intermedium);
						return intermedium.getReferenceObject();
					}
				};
			}

		};

	}
}
