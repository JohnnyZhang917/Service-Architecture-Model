package eu.pmsoft.sam.architecture.model;

import com.google.inject.Key;

import java.util.Set;

@Deprecated
public interface SamService {

    public ServiceKey getServiceKey();

    public Set<Key<?>> getServiceContractAPI();

}
