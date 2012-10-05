package pmsoft.sam.module.see.local;

import pmsoft.sam.module.see.ServiceExecutionEnviroment;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;

public class SEELocalModule extends PrivateModule {

	@Override
	protected void configure() {
//		bind(ServiceExecutionEnviromentInternalAPI.class).to(ServiceExecutionEnviromentLocal.class).asEagerSingleton();
//		install(new FactoryModuleBuilder()
//				.implement(ServiceInstanceConfiguration.class, ServiceInstanceConfiguration.class)
//				.implement(ServiceExternalInstanceConfiguration.class,ServiceExternalInstanceConfiguration.class)
//				.implement(TransactionConfigurator.class, TransactionConfiguratorModel.class)
//				.implement(SamTransaction.class, TransactionInstance.class)
//				.build(TransactionDomainFactory.class));
//		expose(ServiceExecutionEnviroment.class);
	}
	
	@Provides
	public ServiceExecutionEnviroment getExternalApi(ServiceExecutionEnviromentInternalAPI internal){
		return internal;
	}

}
