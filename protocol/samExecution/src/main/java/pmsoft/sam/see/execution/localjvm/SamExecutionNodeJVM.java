package pmsoft.sam.see.execution.localjvm;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.inject.name.Named;
import pmsoft.exceptions.OperationContext;
import pmsoft.exceptions.OperationReportingFactory;
import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.protocol.CanonicalProtocolExecutionContext;
import pmsoft.sam.protocol.CanonicalProtocolInfrastructure;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.api.SamArchitectureRegistry;
import pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import pmsoft.sam.see.api.SamServiceDiscovery;
import pmsoft.sam.see.api.model.*;
import pmsoft.sam.see.api.transaction.BindPointSIID;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import pmsoft.sam.see.api.transaction.SamInjectionModelVisitorAdapter;
import pmsoft.sam.see.injectionUtils.ServiceInjectionUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

public class SamExecutionNodeJVM extends SamServiceRegistryLocal implements SamExecutionNodeInternalApi {

	private final Map<SIID, SamServiceInstanceObject> runningInstances = Maps.newHashMap();
	private final Multimap<SamServiceImplementationKey, SIID> typeOfRunningInstance = HashMultimap.create();
	private final Map<SIURL, SamInstanceTransaction> transactions = Maps.newHashMap();
	private final CanonicalProtocolInfrastructure canonicalProtocol;
    private final InetSocketAddress serverAddress;
    private final OperationReportingFactory operationReportingFactory;

	@Inject
	public SamExecutionNodeJVM(SamArchitectureRegistry architectureRegistry, SamServiceDiscovery serviceDiscoveryRegistry,
                               CanonicalProtocolInfrastructure canonicalProtocol, @Named(SEEServer.SERVER_ADDRESS_BIND) InetSocketAddress serverAddress, OperationReportingFactory operationReportingFactory) {
		super(architectureRegistry, serviceDiscoveryRegistry);
		this.canonicalProtocol = canonicalProtocol;
        this.serverAddress = serverAddress;
        this.operationReportingFactory = operationReportingFactory;
    }

    @Override
    public SamServiceInstance getInternalServiceInstance(SIID exposedService) {
        return runningInstances.get(exposedService);
    }


    @Override
	public CanonicalProtocolExecutionContext createTransactionExecutionContext(SIURL url) {
		Preconditions.checkNotNull(url);
		SamInstanceTransaction transaction = getTransaction(url);
		CanonicalProtocolExecutionContext executionContext = canonicalProtocol.createExecutionContext(transaction);
		protocolExecutionContext.put(url, executionContext.getContextUniqueID(), executionContext);
		return executionContext;
	}

	private final Table<SIURL, UUID, CanonicalProtocolExecutionContext> protocolExecutionContext = HashBasedTable.create();

    //TODO service live cycle, to open/close transacitons
	@Override
	public CanonicalProtocolExecutionContext openTransactionExecutionContext(SIURL targetUrl, UUID transactionUniqueId) {
		Preconditions.checkNotNull(transactionUniqueId);
		if (protocolExecutionContext.contains(targetUrl, transactionUniqueId)) {
			return protocolExecutionContext.get(targetUrl, transactionUniqueId);
		}
		SamInstanceTransaction transaction = getTransaction(targetUrl);
		Preconditions.checkNotNull(transaction, "Request to non registered transaction, URL: %s", targetUrl);
		CanonicalProtocolExecutionContext executionContext = canonicalProtocol.bindExecutionContext(transaction, transactionUniqueId);
		protocolExecutionContext.put(targetUrl, transactionUniqueId, executionContext);
		return executionContext;
	}

    int counter = 0;
	@Override
	public SIURL setupInjectionTransaction(SamInjectionConfiguration configuration, ExecutionStrategy executionStrategy) {
        OperationContext operationContext = operationReportingFactory.openExistingContext();
        SIURL url = null;
        try {
            //TODO create a real service registry related to the server bind port and address, maybe in configuration must be set
            url = SIURL.fromUrlString("http://" + serverAddress.getHostName() + ":" + serverAddress.getPort() + "/service" + counter++);
        } catch (MalformedURLException e) {
            operationContext.getErrors().addError(e,"Error creating service address");
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
                    if( ! samServiceInstanceObject.getServiceKeyContract().equals(contract) ){
                        errors.add("SIID " + bindPointSIID.getSiid() + " have a different contract ");
                    }
                }
                super.visit(bindPointSIID);
            }
        });
		if (!errors.isEmpty()) {
			// TODO a business logic exceptions
			throw new RuntimeException(Joiner.on("\n").join(errors.iterator()));
		}
	}

	@Override
	public SamInstanceTransaction getTransaction(SIURL url) {
        Preconditions.checkArgument(transactions.containsKey(url), "no transaction for url %s",url);
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
		Preconditions.checkArgument(key != null);
		SamServiceImplementation serviceImplementation = getImplementation(key);
		Preconditions.checkNotNull(serviceImplementation, "Service Implementation not found for key [%s]", key);

		SIID id = new SIID();
		Injector injector = ServiceInjectionUtils.createServiceInstanceInjector(serviceImplementation, architectureRegistry);
		SamService contractService = architectureRegistry.getService(serviceImplementation.getSpecificationKey());
		ServiceMetadata finalMetadata = metadata == null ? new ServiceMetadata() : metadata;
		SamServiceInstanceObject instance = new SamServiceInstanceObject(key, id, injector, finalMetadata, contractService.getServiceContractAPI(),
				contractService.getServiceKey());
		runningInstances.put(id, instance);
		typeOfRunningInstance.put(key, id);
		return instance;
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
