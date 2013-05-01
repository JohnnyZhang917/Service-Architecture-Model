package eu.pmsoft.sam.see.api.model;

import com.google.inject.Module;
import eu.pmsoft.sam.architecture.model.ServiceKey;

import java.util.List;

/**
 * Definition of a Service Implementation
 *
 * @author pawel
 */
public interface SamServiceImplementationDeprecated {

    /**
     * Implementation injection configuration provided as a Guice module
     *
     * @return class defining implementation module
     */
    public Class<? extends Module> getModule();

    /**
     * Identification key of implementation
     *
     * @return key of the implementation
     */
    public SamServiceImplementationKey getKey();

    /**
     * Service contract provided by this service
     *
     * @return key of the service contract
     */
    public ServiceKey getSpecificationKey();

    /**
     * List of binded services in natural order
     *
     * @return list of used services keys
     */
    public List<ServiceKey> getBindedServices();
}
