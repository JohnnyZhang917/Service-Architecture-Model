package eu.pmsoft.sam.architecture.model;

import eu.pmsoft.sam.definition.service.SamServiceDefinition;

/**
 * Reference to a service definition.
 * <p/>
 * For service implementation declaration it is necessary to order on the set of
 * binded Services. This order is defined on the ServiceKey set by
 * implementation of Comparable<ServiceKey>.
 *
 * @author pawel
 */
public class ServiceKey {

    private final String serviceDefinitionSignature;

    public ServiceKey(Class<? extends SamServiceDefinition> definitionClass) {
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

        ServiceKey that = (ServiceKey) o;

        if (serviceDefinitionSignature != null ? !serviceDefinitionSignature.equals(that.serviceDefinitionSignature) : that.serviceDefinitionSignature != null)
            return false;

        return true;
    }
}
