package pmsoft.sam.see.api.transaction;

import java.util.Map;
import java.util.Set;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInjectionTransaction;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SamTransactionGrammar {
	// A grammar based on ServiceImplementation definition.
	// InjectionConfiguration: ExposedContract BindingConfiguration*
	// ExposedServiceInstance
	// BindingConfiguration: ExtrenalContract -> SIID | ExtrenalContract ->
	// SIURL | InjectionConfiguration
	//

	public static SamInjectionTransactionGrammarContract createTransactionOn(Class<? extends SamServiceDefinition> serviceContract) {
		return new SamInjectionTransactionGrammarContractBuilder(serviceContract);
	}

	static public interface SamInjectionTransactionGrammarContract {
		public SamInjectionTransactionGrammarContract idBinding(Class<? extends SamServiceDefinition> bindedService,
				SIID instanceId);

		public SamInjectionTransactionGrammarContract urlBinding(Class<? extends SamServiceDefinition> bindedService,
				SIURL instanceUrl);

		public SamInjectionTransactionGrammarContract transactionBinding(SamInjectionTransaction transaction);

		public SamInjectionTransaction providedByServiceInstance(SIID serviceInstance);
	}

	static public class SamInjectionTransactionGrammarContractBuilder implements SamInjectionTransactionGrammarContract {

		private final ServiceKey exposedService;
		private final Set<ServiceKey> registeredKeys = Sets.newHashSet();
		private final Map<ServiceKey, SIID> siidMapping = Maps.newHashMap();
		private final Map<ServiceKey, SIURL> siurlMapping = Maps.newHashMap();
		private final Map<ServiceKey, SamInjectionTransaction> transactionMapping = Maps.newHashMap();
		private boolean definitionOpen = true;

		public SamInjectionTransactionGrammarContractBuilder(Class<? extends SamServiceDefinition> exposedContract) {
			this.exposedService = new ServiceKey(exposedContract);
		}

		@Override
		public SamInjectionTransactionGrammarContract idBinding(Class<? extends SamServiceDefinition> bindedService,
				SIID instanceId) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			ServiceKey key = loadKey(bindedService);
			siidMapping.put(key, instanceId);
			return this;
		}

		private ServiceKey loadKey(Class<? extends SamServiceDefinition> bindedService) {
			ServiceKey serviceKey = new ServiceKey(bindedService);
			Preconditions.checkState(registeredKeys.add(serviceKey));
			return serviceKey;
		}

		@Override
		public SamInjectionTransactionGrammarContract urlBinding(Class<? extends SamServiceDefinition> bindedService,
				SIURL instanceUrl) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			ServiceKey key = loadKey(bindedService);
			siurlMapping.put(key, instanceUrl);
			return this;
		}

		@Override
		public SamInjectionTransactionGrammarContract transactionBinding(SamInjectionTransaction transaction) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			ServiceKey key = transaction.getProvidedService();
			Preconditions.checkState(registeredKeys.add(key));
			transactionMapping.put(key, transaction);
			return this;
		}

		@Override
		public SamInjectionTransaction providedByServiceInstance(SIID serviceInstance) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			definitionOpen = false;
			SamInjectionTransactionObject transaction = new SamInjectionTransactionObject(exposedService, serviceInstance,
					ImmutableMap.copyOf(siidMapping), ImmutableMap.copyOf(siurlMapping), ImmutableMap.copyOf(transactionMapping));
			return transaction;
		}
	}

	static public class SamInjectionTransactionObject implements SamInjectionTransaction {

		private final ServiceKey exposedService;
		private final SIID exposedInstance;
		private ImmutableMap<ServiceKey, SIID> internalMap;
		private ImmutableMap<ServiceKey, SIURL> externalMap;
		private ImmutableMap<ServiceKey, SamInjectionTransaction> nestedMap;

		public SamInjectionTransactionObject(ServiceKey exposedContract, SIID exposedInstance,
				ImmutableMap<ServiceKey, SIID> internalMap, ImmutableMap<ServiceKey, SIURL> externalMap,
				ImmutableMap<ServiceKey, SamInjectionTransaction> nestedMap) {
			this.exposedService = exposedContract;
			this.exposedInstance = exposedInstance;
			this.internalMap = internalMap;
			this.externalMap = externalMap;
			this.nestedMap = nestedMap;
		}

		@Override
		public ServiceKey getProvidedService() {
			return exposedService;
		}

		@Override
		public SIID getExposedServiceInstance() {
			return exposedInstance;
		}

		@Override
		public Map<ServiceKey, SIID> getInternalInjectionConfiguration() {
			return internalMap;
		}

		@Override
		public Map<ServiceKey, SIURL> getExternalInjectionConfiguration() {
			return externalMap;
		}

		@Override
		public Map<ServiceKey, SamInjectionTransaction> getNestedConfiguration() {
			return nestedMap;
		}

	}

}
