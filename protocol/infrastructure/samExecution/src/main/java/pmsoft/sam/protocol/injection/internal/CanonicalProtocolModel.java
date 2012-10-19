package pmsoft.sam.protocol.injection.internal;

import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.testng.collections.Lists;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionService;
import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;
import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionContext;
import pmsoft.sam.protocol.injection.CanonicalProtocolInfrastructure;
import pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInstanceTransaction;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.transaction.BindPointSIID;
import pmsoft.sam.see.api.transaction.BindPointSIURL;
import pmsoft.sam.see.api.transaction.BindPointTransaction;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import pmsoft.sam.see.api.transaction.SamInjectionModelVisitor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

class CanonicalProtocolModel implements CanonicalProtocolInfrastructure {

	private final SamExecutionNodeInternalApi executionNode;

	private final CanonicalProtocolExecutionService executionService;

	@Inject
	public CanonicalProtocolModel(SamExecutionNodeInternalApi executionNode, CanonicalProtocolExecutionService executionService) {
		this.executionNode = executionNode;
		this.executionService = executionService;
	}

	@Override
	public CanonicalProtocolExecutionContext bindExecutionContext(SamInstanceTransaction transaction, UUID transactionUniqueId) {
		return internalCreateExecutionContext(transaction,transactionUniqueId );
	}
	@Override
	public CanonicalProtocolExecutionContext createExecutionContext(SamInstanceTransaction functionContract) {
		return internalCreateExecutionContext(functionContract,UUID.randomUUID());
	}
	
	public CanonicalProtocolExecutionContext internalCreateExecutionContext(final SamInstanceTransaction functionContract, UUID transactionUniqueId) {

		CanonicalProtocolExecutionContext context = functionContract.accept(new SamInjectionModelVisitor<CanonicalProtocolExecutionContext>() {
			private final ImmutableMap.Builder<ExternalBindingController, ExternalInstanceProvider> builder = ImmutableMap.builder();
			private final ImmutableList.Builder<InstanceRegistry> intanceRegistryBuilder = ImmutableList.builder();
			private final ImmutableList.Builder<SIURL> siurlBuilder = ImmutableList.builder();

			private int slotCounter = 0;

			@Override
			public void enterTransaction(SamInstanceTransaction transaction) {
			}

			@Override
			public CanonicalProtocolExecutionContext exitTransaction(SamInstanceTransaction transaction) {
				ImmutableMap<ExternalBindingController, ExternalInstanceProvider> controlToInstanceProviderMap = builder.build();
				TransactionControllerImpl controller = new TransactionControllerImpl(controlToInstanceProviderMap);
				SIURL transactionURL = functionContract.getTransactionURL();
				ImmutableList<SIURL> serviceSlotURL = siurlBuilder.build();
				ImmutableList<InstanceRegistry> serviceSlots = intanceRegistryBuilder.build();
				SIID headSIID = functionContract.getInjectionConfiguration().getExposedServiceInstance();
				Injector realServiceInjector = lookForRealInjector(headSIID);
				Injector glueInjector = createGlueInjector(realServiceInjector, getServiceContract(headSIID));
				InstanceRegistry executionInstanceRegistry = new ServerExecutionInstanceRegistry(realServiceInjector);
				CanonicalProtocolExecutionContextObject mainContext = new CanonicalProtocolExecutionContextObject(transactionURL, serviceSlotURL, serviceSlots,
						executionService, executionInstanceRegistry, controller, glueInjector);
				return mainContext;
			}

			@Override
			public void visit(BindPointTransaction bindPointTransaction) {

			}

			private Injector lookForRealInjector(SIID instance) {
				SamServiceInstance serviceInstance = executionNode.getInternalServiceInstance(instance);
				return serviceInstance.getInjector();
			}

			private Set<Key<?>> getServiceContract(SIID instance) {
				SamServiceInstance serviceInstance = executionNode.getInternalServiceInstance(instance);
				return serviceInstance.getServiceContract();
			}

			private List<InstanceProvider> listOfInstanceProviders = Lists.newArrayList();
			private ExternalBindingController controller;

			@Override
			public void visit(BindPointSIURL bindPointSIURL) {
				InstanceRegistry instanceRegistry = new ClientRecordingInstanceRegistry(slotCounter++);
				intanceRegistryBuilder.add(instanceRegistry);
				siurlBuilder.add(bindPointSIURL.getSiurl());
				RecordingInstanceProvider externalProvider = new RecordingInstanceProvider(instanceRegistry);
				listOfInstanceProviders.add(externalProvider);
			}

			@Override
			public void visit(BindPointSIID bindPointSIID) {
				SIID siid = bindPointSIID.getSiid();
				Injector directServiceInjector = lookForRealInjector(siid);
				Set<Key<?>> serviceApiKeys = getServiceContract(siid);
				listOfInstanceProviders.add(new DirectInstanceProvider(directServiceInjector, serviceApiKeys));
			}
			
			private ExternalBindingController getController(SIID siid) {
				Injector directServiceInjector = lookForRealInjector(siid);
				return directServiceInjector.getInstance(Key.get(ExternalBindingController.class));
			}

			Stack<List<InstanceProvider>> stackServiceInjector = new Stack<List<InstanceProvider>>();
			Stack<ExternalBindingController> stackControllers = new Stack<ExternalBindingController>();

			@Override
			public void enterNested(SamInjectionConfiguration samInjectionTransactionConfiguration) {
				stackServiceInjector.push(listOfInstanceProviders);
				stackControllers.push(controller);
				listOfInstanceProviders = Lists.newArrayList();
				controller = getController(samInjectionTransactionConfiguration.getExposedServiceInstance());
			}

			@Override
			public void exitNested(SamInjectionConfiguration samInjectionTransactionConfiguration) {
				if (samInjectionTransactionConfiguration.hasBindingPoints()) {
					InstanceProviderTransactionContext serviceContext = new InstanceProviderTransactionContext(listOfInstanceProviders);
					builder.put(controller, serviceContext);
				}
				listOfInstanceProviders = stackServiceInjector.pop();
				controller = stackControllers.pop();
			}

		});
		return context;
	}

	private static Injector createGlueInjector(final Injector realInjector, final Set<Key<?>> serviceContracts) {
		Module glueModule = new AbstractModule() {

			@Override
			protected void configure() {
				binder().requireExplicitBindings();
				for (Key<?> key : serviceContracts) {
					createGlueBinding(key, realInjector);
				}
			}

			private <T> void createGlueBinding(Key<T> key, Injector realInjector) {
				bind(key).toProvider(realInjector.getProvider(key));
			}
		};
		return Guice.createInjector(glueModule);
	}

}
