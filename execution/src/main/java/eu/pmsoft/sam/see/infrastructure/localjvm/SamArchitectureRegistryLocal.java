package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.collect.Sets;
import eu.pmsoft.sam.architecture.model.SamArchitecture;
import eu.pmsoft.sam.architecture.model.SamServiceDeprecated;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.api.infrastructure.SamArchitectureManagement;

import java.util.Set;

/**
 * The simplest implementation of Architecture Registry, without any validation between registered architectures and model validation.
 * <p/>
 * Use for local execution of fully know service implementations.
 *
 * @author pawel
 */
public class SamArchitectureRegistryLocal implements SamArchitectureManagement {

    private final Set<SamArchitecture> architectures = Sets.newHashSet();

    @Override
    public SamServiceDeprecated getService(ServiceKey serviceKey) {
        for (SamArchitecture arch : architectures) {
            SamServiceDeprecated service = arch.getService(serviceKey);
            if (service != null) {
                return service;
            }
        }
        return null;
    }

    @Override
    public void registerArchitecture(SamArchitecture architecture) {
        architectures.add(architecture);
    }

}
