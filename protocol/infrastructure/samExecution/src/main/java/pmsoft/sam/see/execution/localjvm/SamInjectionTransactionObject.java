package pmsoft.sam.see.execution.localjvm;

import java.util.Map;
import java.util.Set;

import pmsoft.sam.architecture.api.SamArchitectureRegistry;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;
import pmsoft.sam.protocol.injection.DirectInstanceProvider;
import pmsoft.sam.protocol.injection.InstanceProvider;
import pmsoft.sam.protocol.injection.InstanceProviderTransactionContext;
import pmsoft.sam.protocol.injection.TransactionController;
import pmsoft.sam.protocol.injection.TransactionControllerImpl;
import pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.model.ServiceInstanceReference;
import pmsoft.sam.see.api.transaction.SamInjectionTransaction;
import pmsoft.sam.see.api.transaction.SamInjectionTransactionConfiguration;
import pmsoft.sam.see.api.transaction.TransactionExecutionContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.Ordering;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

class SamInjectionTransactionObject implements SamInjectionTransaction {

	private final SamInjectionTransactionConfiguration configuration;
	private final SamExecutionNodeInternalApi executionNode;
	private final SamArchitectureRegistry architecture;

	public SamInjectionTransactionObject(SamInjectionTransactionConfiguration configuration, SamExecutionNodeInternalApi executionNode,
			SamArchitectureRegistry architecture) {
		this.configuration = configuration;
		this.executionNode = executionNode;
		this.architecture = architecture;
	}

	@Override
	public TransactionExecutionContext createExecutionContext() {
		return new TransactionExecutionContextObject();
	}

	private class TransactionExecutionContextObject implements TransactionExecutionContext {

		private final Injector injector;
		private final TransactionController transactionController;

		public TransactionExecutionContextObject() {
			ImmutableListMultimap.Builder<ExternalInstanceProvider, ExternalBindingController> builder = ImmutableListMultimap.builder();
			loadNestedConfiguration(builder, configuration);
			transactionController = new TransactionControllerImpl(builder.build());
			Module transModule = createExecutionContextModule();
			this.injector = Guice.createInjector(transModule);
		}

		private InstanceProvider loadNestedConfiguration(ImmutableListMultimap.Builder<ExternalInstanceProvider, ExternalBindingController> builder,
				SamInjectionTransactionConfiguration configuration) {

			Map<ServiceKey, ServiceInstanceReference> injectionMap = configuration.getInjectionConfiguration();

			if( ! injectionMap.isEmpty() ) {
				// A leaf node in the injection configuration
				ImmutableList.Builder<InstanceProvider> serviceInjector = ImmutableList.builder();
				
				Ordering<ServiceKey> keyOrder = Ordering.natural();
				ImmutableList<ServiceKey> serviceInjectionPoints = keyOrder.immutableSortedCopy(injectionMap.keySet());
				Preconditions.checkState(keyOrder.isStrictlyOrdered(serviceInjectionPoints));
				
				for (ServiceKey serviceKey : serviceInjectionPoints) {
					ServiceInstanceReference reference = injectionMap.get(serviceKey);
					serviceInjector.add(createInstanceProvider(builder, reference));
				}
				InstanceProviderTransactionContext instanceProvider = new InstanceProviderTransactionContext(serviceInjector.build());
				builder.put(instanceProvider, getInternalBindingController(configuration.getExposedServiceInstance()));
			}
			
			return new DirectInstanceProvider(getRealInjector(configuration.getExposedServiceInstance()),getContractKeys(configuration.getProvidedService()));
		}

		private InstanceProvider createInstanceProvider(Builder<ExternalInstanceProvider, ExternalBindingController> builder, ServiceInstanceReference reference) {
			InstanceProvider instanceProvider;
			if (reference instanceof SIID) {
				SIID siid = (SIID) reference;
				SamServiceInstance service = executionNode.getInternalServiceInstance(siid);
				instanceProvider = new DirectInstanceProvider(service.getInjector(), service.getServiceContract());
			} else if (reference instanceof SIURL) {
				throw new IllegalStateException("not implemented yet");
			} else if (reference instanceof SamInjectionTransactionConfiguration) {
				SamInjectionTransactionConfiguration transaction = (SamInjectionTransactionConfiguration) reference;
				instanceProvider = loadNestedConfiguration(builder, transaction);
			} else {
				throw new IllegalStateException("Unknown type of ServiceInstanceReference" + reference);
			}
			return instanceProvider;
		}

		@Override
		public TransactionController getTransactionController() {
			return transactionController;
		}

		private ExternalBindingController getInternalBindingController(SIID service) {
			return getRealInjector(service).getInstance(ExternalBindingController.class);
		}

		private Injector getRealInjector(SIID service) {
			SamServiceInstance instance = executionNode.getInternalServiceInstance(service);
			return instance.getInjector();
		}

		private Set<Key<?>> getContractKeys(ServiceKey providedService) {
			return architecture.getService(providedService).getServiceContractAPI();
		}

		private Module createExecutionContextModule() {
			return new AbstractModule() {

				@Override
				protected void configure() {
					binder().requireExplicitBindings();
					Set<Key<?>> contractKeys = getContractKeys(configuration.getProvidedService());
					Injector realInjector = getRealInjector(configuration.getExposedServiceInstance());
					for (Key<?> key : contractKeys) {
						createGlueBinding(key, realInjector);
					}
				}

				private <T> void createGlueBinding(Key<T> key, Injector realInjector) {
					bind(key).toProvider(realInjector.getProvider(key));
				}
			};
		}

		@Override
		public Injector getInjector() {
			return injector;
		}
	}

}