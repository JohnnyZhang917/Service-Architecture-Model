package eu.pmsoft.ibus.service;

import eu.pmsoft.ibus.service.service.model.ServiceInstance;

/**
 * User: Paweł Cesar Sanjuan Szklarz
 */
public interface IBServiceRegistry {

    public boolean registerServiceInstance(ServiceInstance instance);

}
