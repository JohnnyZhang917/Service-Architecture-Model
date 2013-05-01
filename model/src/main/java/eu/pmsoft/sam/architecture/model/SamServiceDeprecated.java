package eu.pmsoft.sam.architecture.model;

import com.google.inject.Key;

import java.util.Set;

public interface SamServiceDeprecated {

    public ServiceKey getServiceKey();

    public Set<Key<?>> getServiceContractAPI();

}
