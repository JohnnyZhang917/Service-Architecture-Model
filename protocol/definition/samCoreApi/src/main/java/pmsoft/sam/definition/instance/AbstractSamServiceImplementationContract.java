package pmsoft.sam.definition.instance;

import pmsoft.sam.definition.service.SamServiceDefinition;

import com.google.inject.Module;

/**
 * Abstract class to define Service Implementations.
 * 
 * Currently service implementation is mapped to a Guice module.
 *  
 * @author pawel
 *
 */
public class AbstractSamServiceImplementationContract implements SamServiceImplementationContract {

	private final Class<? extends SamServiceDefinition> serviceContract;
	private final Class<? extends Module> serviceImplementationModule;
	private final Class<? extends SamServiceDefinition>[] bindedServices;

	public AbstractSamServiceImplementationContract(Class<? extends SamServiceDefinition> serviceContract, Class<? extends Module> serviceImplementationModule,
			Class<? extends SamServiceDefinition>[] bindedServices) {
		super();
		this.serviceContract = serviceContract;
		this.serviceImplementationModule = serviceImplementationModule;
		this.bindedServices = bindedServices;
	}

	public Class<? extends SamServiceDefinition> getServiceContract() {
		return serviceContract;
	}

	public Class<? extends Module> getServiceImplementationModule() {
		return serviceImplementationModule;
	}

	public Class<? extends SamServiceDefinition>[] getBindedServices() {
		return bindedServices;
	}

	@Override
	public void readContract(SamServiceImplementationContractLoader reader) {
		reader.loadImplementationContract(serviceContract,serviceImplementationModule,bindedServices);
	}
}
