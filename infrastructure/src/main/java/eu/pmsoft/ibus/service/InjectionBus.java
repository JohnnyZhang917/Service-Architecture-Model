package eu.pmsoft.ibus.service;

import eu.pmsoft.ibus.service.service.model.InjectionBusClientCredentials;

/**
 * User: Paweł Cesar Sanjuan Szklarz
 */
public interface InjectionBus {

    public InjectionBusClientContext openServiceContext(InjectionBusClientCredentials credentials);
}
