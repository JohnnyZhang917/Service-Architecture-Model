package eu.pmsoft.see.api.transaction;

import com.google.common.collect.ImmutableList;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.see.api.model.SIID;

/**
 * API to access a existing injection transaction.
 *
 * @author pawel
 */
public interface SamInjectionConfiguration extends SamTransactionModelVisitable {

    /**
     * Service contract provided by this transaction to the client. Only one
     * contract is provided to client.
     *
     * @return key of the service provided by the head of the configuration
     */
    public ServiceKeyDeprecated getProvidedService();

    /**
     * Service Instance providing the main contract exposed by this transaction
     *
     * @return Unique local SIID for the head service instance
     */
    public SIID getExposedServiceInstance();

    /**
     * @return List of bind point declared on this level
     */
    ImmutableList<BindPoint> getBindPoints();

}