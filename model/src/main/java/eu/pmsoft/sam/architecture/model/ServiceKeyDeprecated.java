package eu.pmsoft.sam.architecture.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;

import java.util.Comparator;
import java.util.List;

/**
 * Reference to a service definition.
 * <p/>
 * For service implementation declaration it is necessary to order on the set of
 * binded Services. This order is defined on the ServiceKey set by
 * implementation of Comparable<ServiceKey>.
 *
 * @author pawel
 */
public class ServiceKeyDeprecated {

    private final String serviceDefinitionSignature;

    public ServiceKeyDeprecated(Class<? extends SamServiceDefinition> definitionClass) {
        assert definitionClass != null;
        this.serviceDefinitionSignature = definitionClass.getCanonicalName();
    }

    public String getServiceDefinitionSignature() {
        return serviceDefinitionSignature;
    }

    @Override
    public int hashCode() {
        return serviceDefinitionSignature.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceKeyDeprecated that = (ServiceKeyDeprecated) o;

        if (serviceDefinitionSignature != null ? !serviceDefinitionSignature.equals(that.serviceDefinitionSignature) : that.serviceDefinitionSignature != null)
            return false;

        return true;
    }

    public static final Comparator<ServiceKeyDeprecated> serviceKeyComparator = new Comparator<ServiceKeyDeprecated>() {
        @Override
        public int compare(ServiceKeyDeprecated arg0, ServiceKeyDeprecated arg1) {
            return arg0.getServiceDefinitionSignature().compareTo(arg1.getServiceDefinitionSignature());
        }
    };

    private static final Ordering<ServiceKeyDeprecated> order = Ordering.from(serviceKeyComparator);

    public static ImmutableList<ServiceKeyDeprecated> orderAndRequireUnique(List<ServiceKeyDeprecated> keys) {
        List<ServiceKeyDeprecated> orderedKeys = order.sortedCopy(keys);
        //TODO exception policy
        if (!order.isStrictlyOrdered(orderedKeys)) throw new RuntimeException("not unique keys order");
        return ImmutableList.copyOf(orderedKeys);
    }
}
