package pmsoft.sam.module.see.local.transaction;

import java.net.URL;
import java.util.List;

import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.inject.wrapper.ServiceBindingDefinition;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.transaction.InjectionConfiguration;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.ServiceInstance;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class ServiceInstanceConfiguration extends AbstractInjectionConfiguration {

	private final SIID instanceIID;

	private final TransactionDomainFactory domainFactory;

	@AssistedInject
	public ServiceInstanceConfiguration(@Assisted SIID instanceIID,
			TransactionDomainFactory configurationFactory) {
		super(instanceIID.getServiceSpecification(), instanceIID.getInjectionPoints());
		this.instanceIID = instanceIID;
		this.domainFactory = configurationFactory;
	}

	public void bindExternalInstance(ServiceKey serviceSpecification, URL externalService) {
		ServiceExternalInstanceConfiguration configuration = domainFactory.createExternalServiceConfiguration(
				serviceSpecification, externalService);
		addInjectionConfiguration(serviceSpecification, configuration);
	}

	public InjectionConfiguration bindInstance(SIID bindInstanceIID) {
		Preconditions.checkNotNull(bindInstanceIID);
		ServiceKey specification = bindInstanceIID.getServiceSpecification();
		ServiceInstanceConfiguration configuration = domainFactory.createServiceConfiguration(bindInstanceIID);
		addInjectionConfiguration(specification, configuration);
		return configuration;
	}

	public ServiceBindingDefinition getBindingDefinition(ServiceExecutionEnviroment see, SamArchitecture architecture) {
		Injector realInjector = getInjector(see, architecture, instanceIID);
		List<Key<?>> bindingKeys = getServiceSpecificationBindings(see, architecture, getServiceSpecification());
		return new ServiceBindingDefinition(bindingKeys, realInjector);
	}

	private Injector getInjector(ServiceExecutionEnviroment see, SamArchitecture architecture, SIID iid) {
		ServiceInstance rootInstance = see.getServiceInstance(iid);
		return rootInstance.getInjector();
	}

	@Override
	public String toString() {
		return "ServiceInstanceConfiguration [instanceIID=" + instanceIID + "]";
	}
}