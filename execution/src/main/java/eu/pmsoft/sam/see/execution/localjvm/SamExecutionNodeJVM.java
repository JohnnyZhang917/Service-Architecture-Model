package eu.pmsoft.sam.see.execution.localjvm;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.*;
import eu.pmsoft.exceptions.OperationContext;
import eu.pmsoft.exceptions.OperationReportingFactory;
import eu.pmsoft.exceptions.OperationRuntimeException;
import eu.pmsoft.execution.ServiceEndpointAddressProvider;
import eu.pmsoft.sam.architecture.model.SamService;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.protocol.CanonicalProtocolInfrastructure;
import eu.pmsoft.sam.protocol.freebinding.FreeVariableBindingBuilder;
import eu.pmsoft.sam.see.api.SamArchitectureRegistry;
import eu.pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import eu.pmsoft.sam.see.api.SamServiceDiscovery;
import eu.pmsoft.sam.see.api.SamServiceRegistry;
import eu.pmsoft.sam.see.api.model.*;
import eu.pmsoft.sam.see.api.transaction.BindPointSIID;
import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.sam.see.api.transaction.SamInjectionModelVisitorAdapter;

import java.net.MalformedURLException;
import java.util.*;

public class SamExecutionNodeJVM implements SamExecutionNodeInternalApi {

    private final Map<SIID, SamServiceInstanceObject> runningInstances = Maps.newHashMap();
    private final Multimap<SamServiceImplementationKey, SIID> typeOfRunningInstance = HashMultimap.create();
    private final Map<SIURL, SamInstanceTransaction> transactions = Maps.newHashMap();
    private final CanonicalProtocolInfrastructure canonicalProtocol;

    private final SamServiceRegistry samServiceRegistry;
    private final SamArchitectureRegistry architectureRegistry;

    private final SamServiceDiscovery serviceDiscoveryRegistry;

    private final OperationReportingFactory operationReportingFactory;

    @Inject
    public SamExecutionNodeJVM(CanonicalProtocolInfrastructure canonicalProtocol, SamServiceRegistry samServiceRegistry, SamArchitectureRegistry architectureRegistry, SamServiceDiscovery serviceDiscoveryRegistry, OperationReportingFactory operationReportingFactory) {
        this.canonicalProtocol = canonicalProtocol;
        this.samServiceRegistry = samServiceRegistry;
        this.architectureRegistry = architectureRegistry;
        this.serviceDiscoveryRegistry = serviceDiscoveryRegistry;
        this.operationReportingFactory = operationReportingFactory;
    }

    @Override
    public SamServiceInstance getInternalServiceInstance(SIID exposedService) {
        return runningInstances.get(exposedService);
    }


    @Override
    public CanonicalProtocolThreadExecutionContext createTransactionExecutionContext(SIURL url) {
        Preconditions.checkNotNull(url);
        SamInstanceTransaction transaction = getTransaction(url);
        CanonicalProtocolThreadExecutionContext executionContext = canonicalProtocol.createExecutionContext(transaction);
        protocolExecutionContext.put(url, executionContext.getContextUniqueID(), executionContext);
        return executionContext;
    }

    private final Table<SIURL, UUID, CanonicalProtocolThreadExecutionContext> protocolExecutionContext = HashBasedTable.create();

    //TODO service live cycle, to open/close transacitons
    @Override
    public CanonicalProtocolThreadExecutionContext openTransactionExecutionContext(SIURL targetUrl, UUID transactionUniqueId) {
        Preconditions.checkNotNull(transactionUniqueId);
        if (protocolExecutionContext.contains(targetUrl, transactionUniqueId)) {
            return protocolExecutionContext.get(targetUrl, transactionUniqueId);
        }
        SamInstanceTransaction transaction = getTransaction(targetUrl);
        Preconditions.checkNotNull(transaction, "Request to non registered transaction, URL: %s", targetUrl);
        CanonicalProtocolThreadExecutionContext executionContext = canonicalProtocol.bindExecutionContext(transaction, transactionUniqueId);
        protocolExecutionContext.put(targetUrl, transactionUniqueId, executionContext);
        return executionContext;
    }

    int counter = 0;

    @Override
    public SIURL setupInjectionTransaction(SamInjectionConfiguration configuration, ExecutionStrategy executionStrategy, ServiceEndpointAddressProvider serverEndpoint) {
        OperationContext operationContext = operationReportingFactory.openExistingContext();
        String serverAddress = serverEndpoint.getServerEndpointBase();
        SIURL url = null;
        try {
            // TODO change http to some custom protocol name
            url = SIURL.fromUrlString("http://" + serverAddress + "/service" + counter++);
        } catch (MalformedURLException e) {
            operationContext.getErrors().addError(e, "Error creating service address");
        }
        return setupInjectionTransaction(configuration, url, executionStrategy);
    }

    @Override
    public SIURL setupInjectionTransaction(SamInjectionConfiguration configuration, SIURL url, ExecutionStrategy executionStrategy) {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(configuration);
        Preconditions.checkNotNull(executionStrategy);
        Preconditions.checkState(!transactions.containsKey(url));
        SamInstanceTransaction transaction = new SamInjectionTransactionObject(configuration, url, executionStrategy);
        validateTransaction(transaction);
        transactions.put(url, transaction);
        serviceDiscoveryRegistry.serviceTransactionCreated(url, transaction.getInjectionConfiguration().getProvidedService());
        return url;
    }

    private void validateTransaction(SamInstanceTransaction transaction) {
        final List<String> errors = Lists.newArrayList();
        transaction.accept(new SamInjectionModelVisitorAdapter<Void>() {
            @Override
            public void visit(BindPointSIID bindPointSIID) {

                if (!SamExecutionNodeJVM.this.runningInstances.containsKey(bindPointSIID.getSiid())) {
                    errors.add("SIID " + bindPointSIID.getSiid() + " not found");
                } else {
                    SamServiceInstanceObject samServiceInstanceObject = SamExecutionNodeJVM.this.runningInstances.get(bindPointSIID.getSiid());
                    ServiceKey contract = bindPointSIID.getContract();
                    if (!samServiceInstanceObject.getServiceKeyContract().equals(contract)) {
                        errors.add("SIID " + bindPointSIID.getSiid() + " have a different contract ");
                    }
                }
                super.visit(bindPointSIID);
            }
        });
        if (!errors.isEmpty()) {
            // FIXME exceptions
            throw new RuntimeException(Joiner.on("\n").join(errors.iterator()));
        }
    }

    @Override
    public SamInstanceTransaction getTransaction(SIURL url) {
        Preconditions.checkArgument(transactions.containsKey(url), "no transaction for url %s", url);
        return transactions.get(url);
    }

    @Override
    public Set<SamServiceInstance> searchInstance(SamServiceImplementationKey key, ServiceMetadata metadata) {
        Collection<SIID> allCandidate = typeOfRunningInstance.get(key);
        boolean emptyQuery = metadata == null || metadata.isEmpty();
        Builder<SamServiceInstance> builder = ImmutableSet.builder();
        for (SIID siid : allCandidate) {
            SamServiceInstanceObject instance = runningInstances.get(siid);
            if (emptyQuery || instance.getMetadata().match(metadata)) {
                builder.add(instance);
            }
        }
        return builder.build();
    }

    @Override
    public SamServiceInstance createServiceInstance(SamServiceImplementationKey key, ServiceMetadata metadata) {
        OperationContext operationContext = operationReportingFactory.openNestedContext();
        SamServiceInstanceObject instance = null;
        try {
            Preconditions.checkArgument(key != null);

            SamServiceImplementation serviceImplementation = samServiceRegistry.getImplementation(key);
            Preconditions.checkNotNull(serviceImplementation, "Service Implementation not found for key [%s]", key);

            SIID id = new SIID();
            Injector injector = createServiceInstanceInjector(serviceImplementation);
            SamService contractService = architectureRegistry.getService(serviceImplementation.getSpecificationKey());
            ServiceMetadata finalMetadata = metadata == null ? new ServiceMetadata() : metadata;
            instance = new SamServiceInstanceObject(key, id, injector, finalMetadata, contractService.getServiceContractAPI(),
                    contractService.getServiceKey());
            runningInstances.put(id, instance);
            typeOfRunningInstance.put(key, id);

        } catch (OperationRuntimeException operationError) {
            operationContext.getErrors().addError(operationError);
        } finally {
            operationReportingFactory.closeContext(operationContext);
            if (operationContext.getErrors().hasErrors()) {
                throw operationContext.getRuntimeError();
            }
        }
        return instance;
    }

    private Injector createServiceInstanceInjector(SamServiceImplementation serviceImplementation) {
        OperationContext operationContext = operationReportingFactory.openExistingContext();
        Class<? extends Module> implModule = serviceImplementation.getModule();
        Module implModuleInstance;
        try {
            implModuleInstance = implModule.newInstance();
        } catch (InstantiationException e) {
            operationContext.getErrors().addError(e);
            throw operationContext.getRuntimeError();
        } catch (IllegalAccessException e) {
            operationContext.getErrors().addError(e);
            throw operationContext.getRuntimeError();
        }

        List<ServiceKey> injectServices = serviceImplementation.getBindedServices();
        ImmutableList<ServiceKey> orderedInjectServices = ServiceKey.orderAndRequireUnique(injectServices);
        List<List<Key<?>>> injectedFreeVariableBinding = Lists.newLinkedList();
        for (ServiceKey externalServiceKey : orderedInjectServices) {
            List<Key<?>> serviceKeys = Lists.newArrayList();
            SamService externalServiceSpec = architectureRegistry.getService(externalServiceKey);
            Set<Key<?>> keys = externalServiceSpec.getServiceContractAPI();
            for (Key<?> serviceApi : keys) {
                serviceKeys.add(serviceApi);
            }
            injectedFreeVariableBinding.add(serviceKeys);
        }
        Module freeVariableModule = FreeVariableBindingBuilder.createFreeBindingModule(injectedFreeVariableBinding);
        Injector implInjector = Guice.createInjector(implModuleInstance, freeVariableModule);
        validateInjectorWithServiceContract(implInjector, serviceImplementation);
        return implInjector;

    }

    private void validateInjectorWithServiceContract(Injector implInjector, SamServiceImplementation serviceImplementation) {
        OperationContext operationContext = operationReportingFactory.openExistingContext();
        SamService externalServiceSpec = architectureRegistry.getService(serviceImplementation.getSpecificationKey());
        Set<Key<?>> contract = externalServiceSpec.getServiceContractAPI();
        for (Key<?> key : contract) {
            if (implInjector.getExistingBinding(key) == null) {
                operationContext.getErrors().addError("missing implementation fo key %s on service implementation %s declared to implement contract %s", key, serviceImplementation, serviceImplementation.getSpecificationKey());
            }
        }
    }

    private static class SamServiceInstanceObject implements SamServiceInstance {

        private final SIID id;
        private final Injector injector;
        private final ServiceMetadata metadata;
        private final ImmutableSet<Key<?>> contract;
        private final ServiceKey serviceKey;
        private final SamServiceImplementationKey implementationKey;

        SamServiceInstanceObject(SamServiceImplementationKey implementationKey, SIID id, Injector injector, ServiceMetadata metadata,
                                 Set<Key<?>> contract, ServiceKey serviceKey) {
            super();
            this.implementationKey = implementationKey;
            this.id = id;
            this.injector = injector;
            this.metadata = metadata;
            this.contract = ImmutableSet.copyOf(contract);
            this.serviceKey = serviceKey;
        }

        @Override
        public SamServiceImplementationKey getImplementationKey() {
            return implementationKey;
        }

        @Override
        public Injector getInjector() {
            return injector;
        }

        @Override
        public SIID getKey() {
            return id;
        }

        @Override
        public ServiceMetadata getMetadata() {
            return metadata;
        }

        @Override
        public Set<Key<?>> getServiceContract() {
            return contract;
        }

        @Override
        public ServiceKey getServiceKeyContract() {
            return serviceKey;
        }

    }
}
