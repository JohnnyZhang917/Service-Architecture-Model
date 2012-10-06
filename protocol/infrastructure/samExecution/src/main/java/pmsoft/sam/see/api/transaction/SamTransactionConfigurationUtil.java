package pmsoft.sam.see.api.transaction;

import java.util.Map;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.ServiceInstanceReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class SamTransactionConfigurationUtil {

	public static SamInjectionTransactionDefinitionGrammar createTransactionOn(Class<? extends SamServiceDefinition> serviceContract) {
		return createTransactionOn(new ServiceKey(serviceContract));
	}

	public static SamInjectionTransactionDefinitionGrammar createTransactionOn(ServiceKey serviceContract) {
		return new SamInjectionTransactionGrammarContractBuilder(serviceContract);
	}

	static public class SamInjectionTransactionGrammarContractBuilder implements SamInjectionTransactionDefinitionGrammar {

		private final ServiceKey exposedService;
		private final Map<ServiceKey, ServiceInstanceReference> references = Maps.newHashMap();
		private boolean definitionOpen = true;

		public SamInjectionTransactionGrammarContractBuilder(ServiceKey exposedContract) {
			this.exposedService = exposedContract;
		}

		private SamInjectionTransactionDefinitionGrammar loadReference(ServiceKey key, ServiceInstanceReference instanceReference) {
			Preconditions.checkState(!references.containsKey(instanceReference));
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			references.put(key, instanceReference);
			return this;
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar idBinding(Class<? extends SamServiceDefinition> bindedService, SIID instanceId) {
			ServiceKey serviceKey = new ServiceKey(bindedService);
			return loadReference(serviceKey, instanceId);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar idBinding(ServiceKey key, SIID instanceId) {
			return loadReference(key, instanceId);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar urlBinding(Class<? extends SamServiceDefinition> bindedService, SIURL instanceUrl) {
			ServiceKey serviceKey = new ServiceKey(bindedService);
			return loadReference(serviceKey, instanceUrl);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar urlBinding(ServiceKey key, SIURL instanceUrl) {
			return loadReference(key, instanceUrl);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar transactionBinding(SamInjectionTransactionConfiguration transaction) {
			return loadReference(transaction.getProvidedService(), transaction);
		}

		@Override
		public SamInjectionTransactionConfiguration providedByServiceInstance(SIID serviceInstance) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			definitionOpen = false;
			SamInjectionTransactionObject transaction = new SamInjectionTransactionObject(exposedService, serviceInstance, ImmutableMap.copyOf(references));
			return transaction;
		}
	}

	static public class SamInjectionTransactionObject implements SamInjectionTransactionConfiguration {

		private final ServiceKey exposedService;
		private final SIID exposedInstance;
		private final ImmutableMap<ServiceKey, ServiceInstanceReference> references;

		public SamInjectionTransactionObject(ServiceKey exposedService, SIID exposedInstance, ImmutableMap<ServiceKey, ServiceInstanceReference> references) {
			super();
			this.exposedService = exposedService;
			this.exposedInstance = exposedInstance;
			this.references = references;
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
		public Map<ServiceKey, ServiceInstanceReference> getInjectionConfiguration() {
			return references;
		}

	}

}
