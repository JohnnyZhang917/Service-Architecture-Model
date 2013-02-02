package pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.collect.Sets;
import pmsoft.exceptions.OperationRuntimeException;
import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.SamArchitectureManagement;

import java.util.Set;

/**
 * The simplest implementation of Architecture Registry, without any validation between registered architectures and model validation.
 * 
 * Use for local execution of fully know service implementations.
 * @author pawel
 *
 */
public class SamArchitectureRegistryLocal implements SamArchitectureManagement {

	private final Set<SamArchitecture> architectures = Sets.newHashSet();
	
	@Override
	public SamService getService(ServiceKey serviceKey) {
        // TODO merge architectures definitions
		for (SamArchitecture arch : architectures) {
			SamService service = arch.getService(serviceKey);
			if( service != null) {
				return service;
			}
		}
		throw new OperationRuntimeException("service not found on architecture" + serviceKey);
	}

	@Override
	public void registerArchitecture(SamArchitecture architecture) {
		architectures.add(architecture);
	}

}
