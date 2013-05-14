package eu.pmsoft.see.api.model;

import com.google.inject.Module;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;

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
    public ServiceKeyDeprecated getSpecificationKey();

    /**
     * List of binded services in natural order
     *
     * @return list of used services keys
     */
    public List<ServiceKeyDeprecated> getBindedServices();
}
