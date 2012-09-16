package pmsoft.sam.module.see.local;

import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.local.transaction.ServiceExternalInstanceConfiguration;
import pmsoft.sam.module.see.local.transaction.ServiceInstanceConfiguration;
import pmsoft.sam.module.see.local.transaction.TransactionConfiguratorModel;
import pmsoft.sam.module.see.local.transaction.TransactionDomainFactory;
import pmsoft.sam.module.see.local.transaction.TransactionInstance;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class SEELocalModule extends PrivateModule {

	@Override
	protected void configure() {
		bind(ServiceExecutionEnviromentInternalAPI.class).to(ServiceExecutionEnviromentLocal.class).asEagerSingleton();
		install(new FactoryModuleBuilder()
				.implement(ServiceInstanceConfiguration.class, ServiceInstanceConfiguration.class)
				.implement(ServiceExternalInstanceConfiguration.class,ServiceExternalInstanceConfiguration.class)
				.implement(TransactionConfigurator.class, TransactionConfiguratorModel.class)
				.implement(SamTransaction.class, TransactionInstance.class)
				.build(TransactionDomainFactory.class));
		expose(ServiceExecutionEnviroment.class);
	}
	
	@Provides
	public ServiceExecutionEnviroment getExternalApi(ServiceExecutionEnviromentInternalAPI internal){
		return internal;
	}

}
