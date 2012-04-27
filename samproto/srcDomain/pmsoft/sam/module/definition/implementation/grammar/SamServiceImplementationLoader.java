package pmsoft.sam.module.definition.implementation.grammar;

import java.util.List;

import pmsoft.sam.module.model.ServiceKey;

import com.google.inject.Module;

public interface SamServiceImplementationLoader {

	/**
	 * Final service implementation definition call.
	 * 
	 * @param serviceSpecification
	 *            implemented service specification
	 * @param implementationModule
	 *            Module defining binding for the service specification
	 *            interfaces
	 * @param withAccessToServices
	 *            List of services injected in the service implementation
	 */
	public void registerImplementation(ServiceKey serviceSpecification, Class<? extends Module> implementationModule,
			List<ServiceKey> withAccessToServices);

}
