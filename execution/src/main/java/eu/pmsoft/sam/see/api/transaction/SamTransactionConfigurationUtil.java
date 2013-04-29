package eu.pmsoft.sam.see.api.transaction;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;
import eu.pmsoft.sam.see.api.model.SIID;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.model.STID;

import java.util.Set;

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
        public SamInjectionTransactionDefinitionGrammar idBinding(Class<? extends SamServiceDefinition> bindService, SIID instanceId) {
            ServiceKey serviceKey = new ServiceKey(bindService);
            return idBinding(serviceKey, instanceId);
        }

        @Override
        public SamInjectionTransactionDefinitionGrammar idBinding(ServiceKey key, SIID instanceId) {
            Preconditions.checkNotNull(instanceId);
            Preconditions.checkNotNull(key);
            return loadReference(key, new BindPointSIID(key, instanceId));
        }

        @Override
        public SamInjectionTransactionDefinitionGrammar urlBinding(Class<? extends SamServiceDefinition> bindService, SIURL instanceUrl) {
            ServiceKey serviceKey = new ServiceKey(bindService);
            return urlBinding(serviceKey, instanceUrl);
        }

        @Override
        public SamInjectionTransactionDefinitionGrammar urlBinding(ServiceKey key, SIURL instanceUrl) {
            Preconditions.checkNotNull(key);
            Preconditions.checkNotNull(instanceUrl);
            return loadReference(key, new BindPointSIURL(key, instanceUrl));
        }

        @Override
        public SamInjectionTransactionDefinitionGrammar nestedTransactionBinding(SamInjectionConfiguration transaction) {
            return loadReference(transaction.getProvidedService(), new BindPointNestedTransaction(transaction));
        }

        @Override
        public SamInjectionConfiguration providedByServiceInstance(SIID serviceInstance) {
            Preconditions.checkNotNull(serviceInstance);

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
