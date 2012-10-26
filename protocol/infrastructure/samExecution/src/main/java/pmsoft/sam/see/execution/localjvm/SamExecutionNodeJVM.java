package pmsoft.sam.see.execution.localjvm;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.protocol.execution.ServiceExecutionEnvironment;
import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionContext;
import pmsoft.sam.protocol.injection.CanonicalProtocolInfrastructure;
import pmsoft.sam.see.api.SamArchitectureRegistry;
import pmsoft.sam.see.api.SamExecutionNodeInternalApi;
import pmsoft.sam.see.api.SamServiceDiscovery;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInstanceTransaction;
import pmsoft.sam.see.api.model.SamServiceImplementation;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.model.ServiceMetadata;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import pmsoft.sam.see.injectionUtils.ServiceInjectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
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
	private final ServiceExecutionEnvironment serviceExecutionEnvironment;

	@Override
	public SamServiceInstance getInternalServiceInstance(SIID exposedService) {
		return runningInstances.get(exposedService);
	}
	
	@Inject
	public SamExecutionNodeJVM(SamArchitectureRegistry architectureRegistry, SamServiceDiscovery serviceDiscoveryRegistry,CanonicalProtocolInfrastructure canonicalProtocol,
			ServiceExecutionEnvironment serviceExecutionEnvironment) {
		super(architectureRegistry, serviceDiscoveryRegistry);
		this.canonicalProtocol = canonicalProtocol;
		this.serviceExecutionEnvironment = serviceExecutionEnvironment;
	}
	
	@Override
	public CanonicalProtocolExecutionContext createTransactionExecutionContext(SIURL url) {
		Preconditions.checkNotNull(url);
		SamInstanceTransaction transaction = getTransaction(url);
		return canonicalProtocol.createExecutionContext(transaction);
	}
	
	private final Table<SIURL, UUID, CanonicalProtocolExecutionContext> protocolExecutionContext = HashBasedTable.create();
	
	@Override
	public CanonicalProtocolExecutionContext openTransactionExecutionContext(SIURL targetUrl, UUID transactionUniqueId) {
		Preconditions.checkNotNull(transactionUniqueId);
		if(protocolExecutionContext.contains(targetUrl, transactionUniqueId)) {
			return protocolExecutionContext.get(targetUrl, transactionUniqueId);
		}
		SamInstanceTransaction transaction = getTransaction(targetUrl);
		Preconditions.checkNotNull(transaction, "Request to non registered transaction, URL: %s", targetUrl);
		CanonicalProtocolExecutionContext executionContext = canonicalProtocol.bindExecutionContext(transaction,transactionUniqueId);
		protocolExecutionContext.put(targetUrl, transactionUniqueId, executionContext);
		return executionContext;
	}

	@Override
	public SIURL setupInjectionTransaction(SamInjectionConfiguration configuration) {
		SIURL url = serviceExecutionEnvironment.createUniqueURL();
		return setupInjectionTransaction(configuration,url);
	}
	
	@Override
	public SIURL setupInjectionTransaction(SamInjectionConfiguration configuration, SIURL url) {
		//TODO create a easily testable API without exposition of url
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(configuration);
		Preconditions.checkState(!transactions.containsKey(url));
		SamInjectionTransactionObject transaction = new SamInjectionTransactionObject(configuration,url);
		transactions.put(url, transaction);
		serviceDiscoveryRegistry.serviceTransactionCreated(url, transaction.getInjectionConfiguration().getProvidedService());
		return url;
	}

	@Override
	public SamInstanceTransaction getTransaction(SIURL url) {
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
		SamServiceInstanceObject instance = new SamServiceInstanceObject(id, injector, finalMetadata,contractService.getServiceContractAPI(),contractService.getServiceKey());
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

		public SamServiceInstanceObject(SIID id, Injector injector, ServiceMetadata metadata, Set<Key<?>> contract,ServiceKey serviceKey) {
			super();
			this.id = id;
			this.injector = injector;
			this.metadata = metadata;
			this.contract = ImmutableSet.copyOf(contract);
			this.serviceKey = serviceKey;
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
