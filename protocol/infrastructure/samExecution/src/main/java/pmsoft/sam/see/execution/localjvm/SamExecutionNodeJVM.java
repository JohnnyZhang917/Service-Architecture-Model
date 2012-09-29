package pmsoft.sam.see.execution.localjvm;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import pmsoft.sam.architecture.api.SamArchitectureRegistry;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.SamServiceRegistry;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SamServiceImplementation;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.model.ServiceInstanceMetadata;
import pmsoft.sam.see.injectionUtils.ServiceInjectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class SamExecutionNodeJVM implements SamExecutionNode {

	private final SamServiceRegistry serviceRegistry;
	private final SamArchitectureRegistry architectureService;

	private final Map<SIID, SamServiceInstanceObject> runningInstances = Maps.newHashMap();
	private final Multimap<SamServiceImplementationKey, SIID> typeOfRunningInstance = HashMultimap.create();

	@Inject
	public SamExecutionNodeJVM(SamServiceRegistry serviceRegistry, SamArchitectureRegistry architectureService) {
		this.serviceRegistry = serviceRegistry;
		this.architectureService = architectureService;
	}

	@Override
	public Set<SamServiceInstance> searchInstance(SamServiceImplementationKey key, ServiceInstanceMetadata metadata) {
		Collection<SIID> allCandidate = typeOfRunningInstance.get(key);
		boolean emptyQuery = metadata == null || metadata.isEmpty();
		Builder<SamServiceInstance> builder = ImmutableSet.builder();
		for (SIID siid : allCandidate) {
			SamServiceInstanceObject instance = runningInstances.get(siid);
			if( emptyQuery || instance.getMetadata().match(metadata)) {
				builder.add(instance);
			}
		}
		return builder.build();
	}

	@Override
	public SamServiceInstance createServiceInstance(SamServiceImplementationKey key, ServiceInstanceMetadata metadata) {
		Preconditions.checkArgument(key != null);
		SamServiceImplementation serviceImplementation = serviceRegistry.getImplementation(key);
		Preconditions.checkNotNull(serviceImplementation, "Service Implementation not found for key [%s]", key);

		SIID id = new SIID();
		Injector injector = ServiceInjectionUtils.createServiceInstanceInjector(serviceImplementation, architectureService);
		ServiceInstanceMetadata finalMetadata = metadata == null ? new ServiceInstanceMetadata() : metadata;
		SamServiceInstanceObject instance = new SamServiceInstanceObject(id, injector, finalMetadata);
		runningInstances.put(id, instance);
		typeOfRunningInstance.put(key, id);
		return instance;
	}

	private static class SamServiceInstanceObject implements SamServiceInstance {

		private final SIID id;
		private final Injector injector;
		private final ServiceInstanceMetadata metadata;

		public SamServiceInstanceObject(SIID id, Injector injector, ServiceInstanceMetadata metadata) {
			super();
			this.id = id;
			this.injector = injector;
			this.metadata = metadata;
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
		public ServiceInstanceMetadata getMetadata() {
			return metadata;
		}

	}
}
