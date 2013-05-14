package eu.pmsoft.see.api.model;

import com.google.inject.Injector;
import com.google.inject.Key;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;

import java.util.Set;

public interface SamServiceInstance {

    public Injector getInjector();

    public SIID getKey();

    public ServiceMetadata getMetadata();

    public Set<Key<?>> getServiceContract();

    public ServiceKeyDeprecated getServiceKeyContract();

    public SamServiceImplementationKey getImplementationKey();

}
