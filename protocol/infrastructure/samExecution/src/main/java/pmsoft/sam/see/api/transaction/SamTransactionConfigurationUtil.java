package pmsoft.sam.see.api.transaction;

import java.util.Set;

import org.testng.internal.annotations.Sets;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

public class SamTransactionConfigurationUtil {

	public static SamInjectionTransactionDefinitionGrammar createTransactionOn(Class<? extends SamServiceDefinition> serviceContract) {
		return createTransactionOn(new ServiceKey(serviceContract));
	}

	public static SamInjectionTransactionDefinitionGrammar createTransactionOn(ServiceKey serviceContract) {
		return new SamInjectionTransactionGrammarContractBuilder(serviceContract);
	}

	static public class SamInjectionTransactionGrammarContractBuilder implements SamInjectionTransactionDefinitionGrammar {

		private final ServiceKey exposedService;
		private final ImmutableList.Builder<BindPoint> bindPointBuilder = ImmutableList.builder();
		private final Set<ServiceKey> usedKeys = Sets.newHashSet();
		private boolean definitionOpen = true;

		public SamInjectionTransactionGrammarContractBuilder(ServiceKey exposedContract) {
			this.exposedService = exposedContract;
		}

		private <T> SamInjectionTransactionDefinitionGrammar loadReference(ServiceKey key, BindPoint bindPoint) {
			Preconditions.checkState(!usedKeys.contains(key));
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			usedKeys.add(key);
			bindPointBuilder.add(bindPoint);
			return this;
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar idBinding(Class<? extends SamServiceDefinition> bindedService, SIID instanceId) {
			ServiceKey serviceKey = new ServiceKey(bindedService);
			return idBinding(serviceKey, instanceId);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar idBinding(ServiceKey key, SIID instanceId) {
			return loadReference(key, new BindPointSIID(key, instanceId));
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar urlBinding(Class<? extends SamServiceDefinition> bindedService, SIURL instanceUrl) {
			ServiceKey serviceKey = new ServiceKey(bindedService);
			return urlBinding(serviceKey, instanceUrl);
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar urlBinding(ServiceKey key, SIURL instanceUrl) {
			return loadReference(key, new BindPointSIURL(key, instanceUrl));
		}

		@Override
		public SamInjectionTransactionDefinitionGrammar transactionBinding(SamInjectionConfiguration transaction) {
			return loadReference(transaction.getProvidedService(), new BindPointTransaction(transaction));
		}

		@Override
		public SamInjectionConfiguration providedByServiceInstance(SIID serviceInstance) {
			Preconditions.checkState(definitionOpen, "Definition already build. Dont reuse builder reference");
			definitionOpen = false;
			ImmutableList<BindPoint> bindPoints = bindPointBuilder.build();
			Ordering<BindPoint> order = Ordering.natural();
			ImmutableList<BindPoint> sortedPoints = order.immutableSortedCopy(bindPoints);
			Preconditions.checkState(order.isStrictlyOrdered(sortedPoints), "Injection Configuration have serviceKey with same order possition. Architecture definition ERROR.");
			SamInjectionConfiguration configuration = new SamInjectionConfigurationObject(exposedService, serviceInstance, sortedPoints);
			return configuration;
		}
	}

}
