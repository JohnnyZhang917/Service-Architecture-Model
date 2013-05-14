package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.collect.Sets;
import eu.pmsoft.sam.architecture.model.SamArchitectureDeprecated;
import eu.pmsoft.sam.architecture.model.SamServiceDeprecated;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.see.api.infrastructure.SamArchitectureManagement;

import java.util.Set;

/**
 * The simplest implementation of Architecture Registry, without any validation between registered architectures and model validation.
 * <p/>
 * Use for local execution of fully know service implementations.
 *
 * @author pawel
 */
public class SamArchitectureRegistryLocal implements SamArchitectureManagement {

    private final Set<SamArchitectureDeprecated> architectures = Sets.newHashSet();

    @Override
    public SamServiceDeprecated getService(ServiceKeyDeprecated serviceKeyDeprecated) {
        for (SamArchitectureDeprecated arch : architectures) {
            SamServiceDeprecated service = arch.getService(serviceKeyDeprecated);
            if (service != null) {
                return service;
            }
        }
        return null;
    }

    @Override
    public void registerArchitecture(SamArchitectureDeprecated architecture) {
        architectures.add(architecture);
    }

}
