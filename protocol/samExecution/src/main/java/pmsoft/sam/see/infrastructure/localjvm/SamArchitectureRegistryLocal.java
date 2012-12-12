package pmsoft.sam.see.infrastructure.localjvm;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.SamArchitectureManagement;

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
		for (SamArchitecture arch : architectures) {
			SamService service = arch.getService(serviceKey);
			if( service != null) {
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
