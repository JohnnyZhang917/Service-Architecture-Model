package pmsoft.sam.inject.wrapper;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;
import pmsoft.sam.inject.free.ExternalBindingController;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;

public class WrappingInjectorBuilder {

	private static class WrappingInjectorLoaderImpl implements WrappingInjectorMainLoader {

		private final ServiceBindingDefinition serviceBinding;
		private ImmutableList<WrappingInjectorLoader> installedBindings;

		public WrappingInjectorLoaderImpl(ServiceBindingDefinition serviceBinding) {
			super();
			this.serviceBinding = serviceBinding;
		}

		public List<WrappingInjectorLoader> bindService(List<ServiceBindingDefinition> serviceBindings) {
			// TODO installed bindings list may be empty
			Preconditions.checkState(installedBindings == null);
			Preconditions.checkNotNull(serviceBindings);
			Builder<WrappingInjectorLoader> builder = ImmutableList.builder();
			for (ServiceBindingDefinition serviceBindingDefinition : serviceBindings) {
				WrappingInjectorLoaderImpl install = new WrappingInjectorLoaderImpl(serviceBindingDefinition);
				builder.add(install);
			}
			installedBindings = builder.build();
			return installedBindings;
		}

		public Module createWrappingInjector(CanonicalProtocolExecutionService canonicalExecutionService, URL transactionURL) {
			final WrappingInjectorControllerInternal controller = new WrappingGlobalController(canonicalExecutionService,transactionURL);
			bindGlobalControllerToServiceInstances(controller);
			return new AbstractModule() {

				@Override
				protected void configure() {
					binder().requireExplicitBindings();
					bind(WrappingInjectorController.class).toInstance(controller);
					for (Key<?> exposedKey : serviceBinding.getServiceKeys()) {
						createWrappingBinding(exposedKey, serviceBinding.getRealServiceInjector());
					}
				}

				private <T> void createWrappingBinding(Key<T> exposedKey, Injector wrappedInjector) {
					Provider<T> realProvider = wrappedInjector.getProvider(exposedKey);
					Class<?> referenceClass = exposedKey.getTypeLiteral().getRawType();
					WrappingControllerProvider<T> monitorProvider = new WrappingControllerProvider<T>(realProvider,
							referenceClass, controller);
					bind(exposedKey).toProvider(monitorProvider);
				}

			};
		}

		/**
		 * Create the injection configuration for the wrapped service instance.
		 * External services don't have injection configuration as they are
		 * accessible only to call using the canonical protocol
		 * 
		 * @param contextController
		 */
		private void bindGlobalControllerToServiceInstances(final WrappingInjectorControllerInternal contextController) {
			Injector serviceRealInjector = serviceBinding.getRealServiceInjector();
			if (serviceRealInjector != null) {
				ExternalBindingController internalServiceControl = serviceRealInjector
						.getInstance(ExternalBindingController.class);
				ServiceWrapperConfiguration wrapperConfiguration = new ServiceWrapperConfiguration();
				for (Iterator<WrappingInjectorLoader> iterator = installedBindings.iterator(); iterator.hasNext();) {
					WrappingInjectorLoaderImpl internal = (WrappingInjectorLoaderImpl) iterator.next();
					wrapperConfiguration.registerServiceInstance(internal.serviceBinding, contextController);
					internal.bindGlobalControllerToServiceInstances(contextController);
				}
				contextController.registerServiceInstanceController(internalServiceControl, wrapperConfiguration);
			}
		}

	}

	public static WrappingInjectorMainLoader createLoader(ServiceBindingDefinition serviceBinding) {
		return new WrappingInjectorLoaderImpl(serviceBinding);
	}
}
