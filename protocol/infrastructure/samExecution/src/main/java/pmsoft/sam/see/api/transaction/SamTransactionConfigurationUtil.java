package pmsoft.sam.see.api.transaction;

import java.util.Map;
import java.util.Set;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SamTransactionConfigurationUtil {

	public static SamInjectionTransactionDefinitionGrammar createTransactionOn(Class<? extends SamServiceDefinition> serviceContract) {
		return createTransactionOn(new ServiceKey(serviceContract));
	}

	public static SamInjectionTransactionDefinitionGrammar createTransactionOn(ServiceKey serviceContract) {
		return new SamInjectionTransactionGrammarContractBuilder(serviceContract);
	}

	static public class SamInjectionTransactionGrammarContractBuilder implements SamInjectionTransactionDefinitionGrammar {

		private final ServiceKey exposedService;
		private final Set<ServiceKey> registeredKeys = Sets.newHashSet();
		private final Map<ServiceKey, SIID> siidMapping = Maps.newHashMap();
		private final Map<ServiceKey, SIURL> siurlMapping = Maps.newHashMap();
		private final Map<ServiceKey, SamInjectionTransactionConfiguration> transactionMapping = Maps.newHashMap();
		private boolean definitionOpen = true;

		public SamInjectionTransactionGrammarContractBuilder(ServiceKey exposedContract) {
			this.exposedService = exposedContract;
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar idBinding(Class<? extends SamServiceDefinition> bindedService,
				SIID instanceId) {
			ServiceKey serviceKey = new ServiceKey(bindedService);
			return idBinding(serviceKey, instanceId);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar idBinding(ServiceKey key, SIID instanceId) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			checkKey(key);
			siidMapping.put(key, instanceId);
			return this;
		}

		private void checkKey(ServiceKey serviceKey) {
			Preconditions.checkState(registeredKeys.add(serviceKey));
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar urlBinding(Class<? extends SamServiceDefinition> bindedService,
				SIURL instanceUrl) {
			ServiceKey serviceKey = new ServiceKey(bindedService);
			return urlBinding(serviceKey, instanceUrl);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar urlBinding(ServiceKey key, SIURL instanceUrl) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			checkKey(key);
			siurlMapping.put(key, instanceUrl);
			return this;
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar transactionBinding(SamInjectionTransactionConfiguration transaction) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			ServiceKey key = transaction.getProvidedService();
			Preconditions.checkState(registeredKeys.add(key));
			transactionMapping.put(key, transaction);
			return this;
		}

		@Override
		public SamInjectionTransactionConfiguration providedByServiceInstance(SIID serviceInstance) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			definitionOpen = false;
			SamInjectionTransactionObject transaction = new SamInjectionTransactionObject(exposedService, serviceInstance,
					ImmutableMap.copyOf(siidMapping), ImmutableMap.copyOf(siurlMapping), ImmutableMap.copyOf(transactionMapping));
			return transaction;
		}
	}

	static public class SamInjectionTransactionObject implements SamInjectionTransactionConfiguration {

		private final ServiceKey exposedService;
		private final SIID exposedInstance;
		private ImmutableMap<ServiceKey, SIID> internalMap;
		private ImmutableMap<ServiceKey, SIURL> externalMap;
		private ImmutableMap<ServiceKey, SamInjectionTransactionConfiguration> nestedMap;

		public SamInjectionTransactionObject(ServiceKey exposedContract, SIID exposedInstance,
				ImmutableMap<ServiceKey, SIID> internalMap, ImmutableMap<ServiceKey, SIURL> externalMap,
				ImmutableMap<ServiceKey, SamInjectionTransactionConfiguration> nestedMap) {
			this.exposedService = exposedContract;
			this.exposedInstance = exposedInstance;
			this.internalMap = internalMap;
			this.externalMap = externalMap;
			this.nestedMap = nestedMap;
		}

		@Override
		public String toString() {
			return "SamInjectionTransactionObject [exposedService=" + exposedService + ", exposedInstance=" + exposedInstance
					+ ", internalMap=" + internalMap + ", externalMap=" + externalMap + ", nestedMap=" + nestedMap + "]";
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
		public Map<ServiceKey, SamInjectionTransactionConfiguration> getNestedConfiguration() {
			return nestedMap;
		}

	}

}
