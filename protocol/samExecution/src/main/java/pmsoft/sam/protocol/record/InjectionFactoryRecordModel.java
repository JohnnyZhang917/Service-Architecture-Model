package pmsoft.sam.protocol.record;

import java.util.List;
import java.util.UUID;
import java.net.URL;

import com.google.inject.assistedinject.Assisted;
import pmsoft.sam.protocol.TransactionController;
import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import pmsoft.sam.see.api.model.ExecutionStrategy;
import pmsoft.sam.see.api.model.SamServiceInstance;

interface InjectionFactoryRecordModel {

	CanonicalProtocolExecutionContextObject canonicalProtocolExecutionContextObject(@Assisted("record") MethodRecordContext record, @Assisted("execute")MethodRecordContext execution,
			UUID transactionUniqueId, TransactionController buildController, ImmutableList<URL> endpointAddresses, InjectorReference reference);

	TransactionController transactionController(ImmutableMap<ExternalBindingController, ExternalInstanceProvider> controlToInstanceProviderMap);

	InstanceProviderTransactionContext instanceProviderTransactionContext(List<InstanceProvider> transactionLevelProviders);

	ClientRecordingInstanceRegistry clientRecordingInstanceRegistry(int slotNr);

    ServerExecutionInstanceRegistry serverExecutionInstanceRegistry(SamServiceInstance serviceInstance);

    ProviderExecutionStackManager providerExecutionStackManager(ImmutableList<InstanceRegistry> instanceRegistries, ExecutionStrategy executionStrategy);

    ClientExecutionStackManager clientExecutionStackManager(ImmutableList<InstanceRegistry> instanceRegistries, ExecutionStrategy executionStrategy);

    MethodRecordContext methodRecordContext(ImmutableList<InstanceRegistry> instanceRegistries, AbstractExecutionStackManager executionManager);

}
