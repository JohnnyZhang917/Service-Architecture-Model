package pmsoft.sam.protocol.record;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.protocol.CanonicalProtocolExecutionContext;
import pmsoft.sam.protocol.CanonicalProtocolInfrastructure;
import pmsoft.sam.protocol.TransactionController;
import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;
import pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import pmsoft.sam.see.api.model.*;
import pmsoft.sam.see.api.transaction.*;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

class CanonicalProtocolModel implements CanonicalProtocolInfrastructure {

    private final SamExecutionNodeInternalApi executionNode;

    private final InjectionFactoryRecordModel modelFactory;

    @Inject
    public CanonicalProtocolModel(SamExecutionNodeInternalApi executionNode, InjectionFactoryRecordModel modelFactory) {
        this.executionNode = executionNode;
        this.modelFactory = modelFactory;
    }

    @Override
    public CanonicalProtocolExecutionContext bindExecutionContext(SamInstanceTransaction transaction, UUID transactionUniqueId) {
        return internalCreateExecutionContext(transaction, transactionUniqueId);
    }

    @Override
    public CanonicalProtocolExecutionContext createExecutionContext(SamInstanceTransaction functionContract) {
        return internalCreateExecutionContext(functionContract, UUID.randomUUID());
    }

    public CanonicalProtocolExecutionContext internalCreateExecutionContext(final SamInstanceTransaction functionContract, final UUID transactionUniqueId) {
        CanonicalProtocolExecutionContext context = functionContract.accept(new SamInjectionModelVisitorAdapter<CanonicalProtocolExecutionContext>() {

            private final ImmutableList.Builder<InstanceRegistry> externalRegistryBuilder = ImmutableList.builder();
            private final ImmutableList.Builder<URL> externalAddressBuilder = ImmutableList.builder();

            private final ImmutableMap.Builder<ExternalBindingController, ExternalInstanceProvider> controllerMapBuilder = ImmutableMap.builder();

            private ImmutableList.Builder<InstanceProvider> internalInstanceProviderBuilder = null;

            private int serviceSlotNr = 0;

            @Override
            public CanonicalProtocolExecutionContext visitTransaction(SamInstanceTransaction transaction) {
                super.visitTransaction(transaction);
                SamInjectionConfiguration headInjectionConfiguration = functionContract.getInjectionConfiguration();
                Injector realServiceInjector = lookForRealInjector(headInjectionConfiguration.getExposedServiceInstance());
                Set<Key<?>> headServiceContract = getServiceContract(headInjectionConfiguration.getExposedServiceInstance(),
                        headInjectionConfiguration.getProvidedService());
                Injector glueInjector = createGlueInjector(realServiceInjector, headServiceContract);


                ImmutableMap<ExternalBindingController, ExternalInstanceProvider> controlToInstanceProviderMap = controllerMapBuilder.build();
                TransactionController controller = modelFactory.transactionController(controlToInstanceProviderMap);

                ImmutableList<InstanceRegistry> recordInstanceRegistries = externalRegistryBuilder.build();
                ClientExecutionStackManager executorTail = createClientExecutor(transaction.getExecutionStrategy(), recordInstanceRegistries);
                MethodRecordContext recordContext = modelFactory.methodRecordContext(recordInstanceRegistries, executorTail);

                InstanceRegistry executionInstanceRegistry = modelFactory.serverExecutionInstanceRegistry(getServiceInstance(headInjectionConfiguration.getExposedServiceInstance()));
                ImmutableList<InstanceRegistry> executionInstanceRegistryList = ImmutableList.of(executionInstanceRegistry);
                ProviderExecutionStackManager executorHead = createProviderExecutor(transaction.getExecutionStrategy(), executionInstanceRegistryList);
                MethodRecordContext executionContext = modelFactory.methodRecordContext(executionInstanceRegistryList, executorHead);

                CanonicalProtocolExecutionContextObject buildedContext = modelFactory.canonicalProtocolExecutionContextObject(recordContext, executionContext,
                        transactionUniqueId, controller, externalAddressBuilder.build(), new InjectorReference(glueInjector));
                return buildedContext;
            }

            private ProviderExecutionStackManager createProviderExecutor(ExecutionStrategy executionStrategy, ImmutableList<InstanceRegistry> instanceRegistries) {
                return modelFactory.providerExecutionStackManager(instanceRegistries, executionStrategy);
            }

            private ClientExecutionStackManager createClientExecutor(ExecutionStrategy executionStrategy, ImmutableList<InstanceRegistry> instanceRegistries) {
                return modelFactory.clientExecutionStackManager(instanceRegistries, executionStrategy);
            }


            @Override
            public void visit(SamInjectionConfiguration configuration) {
                Builder<InstanceProvider> currentRegistryBuilder = internalInstanceProviderBuilder;
                internalInstanceProviderBuilder = ImmutableList.builder();

                super.visit(configuration);

                ImmutableList<InstanceProvider> transactionLevelProviders = internalInstanceProviderBuilder.build();
                if (!transactionLevelProviders.isEmpty()) {
                    InstanceProviderTransactionContext serviceContext = modelFactory.instanceProviderTransactionContext(transactionLevelProviders);
                    ExternalBindingController controller = getController(configuration.getExposedServiceInstance());
                    controllerMapBuilder.put(controller, serviceContext);
                }
                internalInstanceProviderBuilder = currentRegistryBuilder;
            }

            @Override
            public void visit(BindPointSIID bindPointSIID) {
                SIID siid = bindPointSIID.getSiid();
                Injector directServiceInjector = lookForRealInjector(siid);
                Set<Key<?>> serviceApiKeys = getServiceContract(siid, bindPointSIID.getContract());
                // not used the modelFactory, because Injector in constructor
                internalInstanceProviderBuilder.add(new DirectInstanceProvider(directServiceInjector, serviceApiKeys));
            }

            @Override
            public void visit(BindPointSIURL bindPointSIURL) {
                SIURL siurl = bindPointSIURL.getSiurl();
                ClientRecordingInstanceRegistry instanceRegistry = modelFactory.clientRecordingInstanceRegistry(serviceSlotNr++);
                externalRegistryBuilder.add(instanceRegistry);
                externalAddressBuilder.add(siurl.getServiceInstanceReference());
                internalInstanceProviderBuilder.add(instanceRegistry);
            }

            @Override
            public void visit(BindPointNestedTransaction bindPointTransaction) {
                super.visit(bindPointTransaction);
            }

            private ExternalBindingController getController(SIID siid) {
                Injector directServiceInjector = lookForRealInjector(siid);
                return directServiceInjector.getInstance(Key.get(ExternalBindingController.class));
            }

            private Injector lookForRealInjector(SIID instance) {
                SamServiceInstance serviceInstance = executionNode.getInternalServiceInstance(instance);
                return serviceInstance.getInjector();
            }

            private SamServiceInstance getServiceInstance(SIID instanceId) {
                return executionNode.getInternalServiceInstance(instanceId);
            }

            private Set<Key<?>> getServiceContract(SIID instance, ServiceKey serviceKey) {
                SamServiceInstance serviceInstance = executionNode.getInternalServiceInstance(instance);
                Preconditions.checkState(
                        serviceInstance.getServiceKeyContract().getServiceDefinitionSignature().compareTo(serviceKey.getServiceDefinitionSignature()) == 0,
                        "ServiceTransaction configuration is mapping a service contract to a instance SIID with other contract. Check configuration");
                return serviceInstance.getServiceContract();
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
