package deprecated.pmsoft.sam.module.see.local.transaction;


public class ServiceInstanceConfiguration {
//extends AbstractInjectionConfiguration {
//
//	private final SIID instanceIID;
//
//	private final TransactionDomainFactory domainFactory;
//
//	@AssistedInject
//	public ServiceInstanceConfiguration(@Assisted SIID instanceIID,
//			TransactionDomainFactory configurationFactory) {
//		super(instanceIID.getServiceSpecification(), instanceIID.getInjectionPoints());
//		this.instanceIID = instanceIID;
//		this.domainFactory = configurationFactory;
//	}
//
//	public void bindExternalInstance(ServiceKey serviceSpecification, URL externalService) {
//		ServiceExternalInstanceConfiguration configuration = domainFactory.createExternalServiceConfiguration(
//				serviceSpecification, externalService);
//		addInjectionConfiguration(serviceSpecification, configuration);
//	}
//
//	public InjectionConfiguration bindInstance(SIID bindInstanceIID) {
//		Preconditions.checkNotNull(bindInstanceIID);
//		ServiceKey specification = bindInstanceIID.getServiceSpecification();
//		ServiceInstanceConfiguration configuration = domainFactory.createServiceConfiguration(bindInstanceIID);
//		addInjectionConfiguration(specification, configuration);
//		return configuration;
//	}
//
//	public ServiceBindingDefinition getBindingDefinition(ServiceExecutionEnviroment see, SamArchitecture architecture) {
//		Injector realInjector = getInjector(see, architecture, instanceIID);
//		List<Key<?>> bindingKeys = getServiceSpecificationBindings(see, architecture, getServiceSpecification());
//		return new ServiceBindingDefinition(bindingKeys, realInjector);
//	}
//
//	private Injector getInjector(ServiceExecutionEnviroment see, SamArchitecture architecture, SIID iid) {
//		ServiceInstance rootInstance = see.getServiceInstance(iid);
//		return rootInstance.getInjector();
//	}
//
//	@Override
//	public String toString() {
//		return "ServiceInstanceConfiguration [instanceIID=" + instanceIID + "]";
//	}
}