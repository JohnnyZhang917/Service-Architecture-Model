package pmsoft.sam.see.execution.localjvm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import pmsoft.sam.architecture.api.SamArchitectureRegistry;
import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.implementation.SamServiceImplementationContractLoader;
import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.see.api.SamServiceDiscovery;
import pmsoft.sam.see.api.SamServiceRegistry;
import pmsoft.sam.see.api.model.SamServiceImplementation;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Module;

public abstract class SamServiceRegistryLocal implements SamServiceRegistry {

	private final Map<SamServiceImplementationKey, ServiceImplementationObject> register = Maps.newHashMap();

	protected final SamArchitectureRegistry architectureRegistry;
	
	protected final SamServiceDiscovery serviceDiscoveryRegistry;

	public SamServiceRegistryLocal(SamArchitectureRegistry architectureRegistry, SamServiceDiscovery serviceDiscoveryRegistry) {
		super();
		this.architectureRegistry = architectureRegistry;
		this.serviceDiscoveryRegistry = serviceDiscoveryRegistry;
	}

	@Override
	public void registerServiceImplementationPackage(SamServiceImplementationPackageContract definition) {
		SamServiceImplementationContractLoaderImpl reader = new SamServiceImplementationContractLoaderImpl();
		definition.loadContractPackage(reader);
		Set<ServiceImplementationObject> implementations = reader.buildImplementations();
		Map<SamServiceImplementationKey, ServiceImplementationObject> registerNew = Maps.newHashMapWithExpectedSize(implementations.size());
		for (ServiceImplementationObject serviceImplementation : implementations) {
			registerNew.put(serviceImplementation.getKey(), serviceImplementation);
		}
		SetView<SamServiceImplementationKey> alreadyExistingKeys = Sets.intersection(registerNew.keySet(), register.keySet());
		Preconditions.checkState(alreadyExistingKeys.size()==0, "Duplicate registration of services with kets [%s]", alreadyExistingKeys);
		register.putAll(registerNew);
	}

	@Override
	public SamServiceImplementation getImplementation(SamServiceImplementationKey key) {
		return register.get(key);
	}
	

	
	private class SamServiceImplementationContractLoaderImpl implements SamServiceImplementationContractLoader {

		private Set<SamServiceImplementationGrammarContractImpl> implementations = Sets.newHashSet();

		Set<ServiceImplementationObject> buildImplementations() {
			ImmutableSet.Builder<ServiceImplementationObject> builder = ImmutableSet.builder();
			for (SamServiceImplementationGrammarContractImpl definition : implementations) {
				builder.add(definition.build());
			}
			return builder.build();
		}

		@Override
		public SamServiceImplementationGrammarContract provideContract(Class<? extends SamServiceDefinition> serviceContract) {
			Preconditions.checkNotNull(serviceContract);
			SamServiceImplementationGrammarContractImpl impl = new SamServiceImplementationGrammarContractImpl(serviceContract);
			implementations.add(impl);
			return impl;
		}

		private class SamServiceImplementationGrammarContractImpl implements SamServiceImplementationGrammarContract {

			private final Class<? extends SamServiceDefinition> serviceContract;
			private final SamService service;
			private final Set<Class<? extends SamServiceDefinition>> bindings = Sets.newHashSet();
			private Class<? extends Module> module = null;

			public SamServiceImplementationGrammarContractImpl(Class<? extends SamServiceDefinition> serviceContract) {
				this.serviceContract = serviceContract;
				ServiceKey key = new ServiceKey(serviceContract);
				this.service = architectureRegistry.getService(key);
				Preconditions.checkNotNull(service, "Service definition [%s] not registered on the Architecture Registry", key);
			}

			public ServiceImplementationObject build() {
				Preconditions.checkState(module != null);
				ServiceKey contract = new ServiceKey(serviceContract);
				ImmutableList.Builder<ServiceKey> binds = ImmutableList.builder();
				for (Class<? extends SamServiceDefinition> serviceBind : bindings) {
					binds.add(new ServiceKey(serviceBind));
				}
				SamServiceImplementationKey key = new SamServiceImplementationKey(module.getName());
				return new ServiceImplementationObject(module, key, contract, binds.build());
			}

			@Override
			public SamServiceImplementationGrammarContract withBindingsTo(Class<? extends SamServiceDefinition> bindedService) {
				Preconditions.checkNotNull(bindedService);
				Preconditions.checkState(module == null);
				Preconditions.checkNotNull(architectureRegistry.getService(new ServiceKey(bindedService)),
						"Service definition [%s] not registered on the Architecture Registry", bindedService);
				bindings.add(bindedService);
				return this;
			}

			@Override
			public void implementedInModule(Class<? extends Module> serviceImplementationModule) {
				Preconditions.checkState(module == null);
				this.module = serviceImplementationModule;
			}

		}
	}

	private static class ServiceImplementationObject implements SamServiceImplementation {

		private final Class<? extends Module> module;
		private final SamServiceImplementationKey key;
		private final ServiceKey contract;
		private final ImmutableList<ServiceKey> binds;

		ServiceImplementationObject(Class<? extends Module> module, SamServiceImplementationKey key, ServiceKey contract,
				ImmutableList<ServiceKey> binds) {
			this.module = module;
			this.key = key;
			this.contract = contract;
			this.binds = binds;
		}

		@Override
		public Class<? extends Module> getModule() {
			return module;
		}

		@Override
		public SamServiceImplementationKey getKey() {
			return key;
		}

		@Override
		public ServiceKey getSpecificationKey() {
			return contract;
		}

		@Override
		public List<ServiceKey> getBindedServices() {
			return binds;
		}

	}
}
