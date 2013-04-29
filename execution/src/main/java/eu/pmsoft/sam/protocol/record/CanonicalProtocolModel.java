package eu.pmsoft.sam.protocol.record;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.protocol.CanonicalProtocolRecordingModel;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.protocol.TransactionController;
import eu.pmsoft.sam.protocol.freebinding.ExternalBindingController;
import eu.pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;
import eu.pmsoft.sam.see.api.setup.SamExecutionNodeInternalApi;
import eu.pmsoft.sam.see.api.model.*;
import eu.pmsoft.sam.see.api.transaction.*;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

class CanonicalProtocolModel implements CanonicalProtocolRecordingModel {

    private final InjectionFactoryRecordModel modelFactory;

    @Inject
    public CanonicalProtocolModel(InjectionFactoryRecordModel modelFactory) {
        this.modelFactory = modelFactory;
    }

    @Override
    public CanonicalProtocolThreadExecutionContext createExecutionContext(SamServiceInstanceTransaction functionContract, SamExecutionNodeInternalApi samExecutionNodeJVM) {
        return internalCreateExecutionContext(functionContract, UUID.randomUUID(),samExecutionNodeJVM);
    }

    @Override
    public CanonicalProtocolThreadExecutionContext bindExecutionContext(SamServiceInstanceTransaction transaction, UUID transactionUniqueId, SamExecutionNodeInternalApi samExecutionNodeJVM) {
        return internalCreateExecutionContext(transaction, transactionUniqueId, samExecutionNodeJVM);
    }


    public CanonicalProtocolThreadExecutionContext internalCreateExecutionContext(final SamServiceInstanceTransaction functionContract, final UUID transactionUniqueId, final SamExecutionNodeInternalApi executionNode) {
        CanonicalProtocolThreadExecutionContext context = functionContract.accept(new SamInjectionModelVisitorAdapter<CanonicalProtocolThreadExecutionContext>() {

            private final ImmutableList.Builder<InstanceRegistry> externalRegistryBuilder = ImmutableList.builder();
            private final ImmutableList.Builder<URL> externalAddressBuilder = ImmutableList.builder();

            private final ImmutableMap.Builder<ExternalBindingController, ExternalInstanceProvider> controllerMapBuilder = ImmutableMap.builder();

            private ImmutableList.Builder<InstanceProvider> internalInstanceProviderBuilder = null;

            private int serviceSlotNr = 0;

            @Override
            public CanonicalProtocolThreadExecutionContext visitTransaction(SamServiceInstanceTransaction transaction) {
                super.visitTransaction(transaction);
                SamInjectionConfiguration headInjectionConfiguration = functionContract.getInjectionConfiguration();
                Injector realServiceInjector = lookForRealInjector(headInjectionConfiguration.getExposedServiceInstance());
                Set<Key<?>> headServiceContract = getServiceContract(headInjectionConfiguration.getExposedServiceInstance(),
                        headInjectionConfiguration.getProvidedService());
                Injector glueInjector = createGlueInjector(realServiceInjector, headServiceContract);


                ImmutableMap<ExternalBindingController, ExternalInstanceProvider> controlToInstanceProviderMap = controllerMapBuilder.build();
                TransactionController controller = modelFactory.transactionController(controlToInstanceProviderMap);

                ImmutableList<InstanceRegistry> recordInstanceRegistries = externalRegistryBuilder.build();
                ClientExecutionStackManager executorTail = createClientExecutor( recordInstanceRegistries);
                MethodRecordContext recordContext = modelFactory.methodRecordContext(recordInstanceRegistries, executorTail);

                InstanceRegistry executionInstanceRegistry = modelFactory.serverExecutionInstanceRegistry(getServiceInstance(headInjectionConfiguration.getExposedServiceInstance()));
                ImmutableList<InstanceRegistry> executionInstanceRegistryList = ImmutableList.of(executionInstanceRegistry);
                ProviderExecutionStackManager executorHead = createProviderExecutor( executionInstanceRegistryList);
                MethodRecordContext executionContext = modelFactory.methodRecordContext(executionInstanceRegistryList, executorHead);

                CanonicalProtocolThreadExecutionContextObject buildedContext = modelFactory.canonicalProtocolExecutionContextObject(recordContext, executionContext,
                        transactionUniqueId, controller, externalAddressBuilder.build(), new InjectorReference(glueInjector));
                return buildedContext;
            }

            private ProviderExecutionStackManager createProviderExecutor( ImmutableList<InstanceRegistry> instanceRegistries) {
                return modelFactory.providerExecutionStackManager(instanceRegistries);
            }

            private ClientExecutionStackManager createClientExecutor( ImmutableList<InstanceRegistry> instanceRegistries) {
                return modelFactory.clientExecutionStackManager(instanceRegistries);
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
