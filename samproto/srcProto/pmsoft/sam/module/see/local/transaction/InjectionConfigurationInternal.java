package pmsoft.sam.module.see.local.transaction;

import java.net.URL;

import com.google.inject.Injector;

import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;
import pmsoft.sam.inject.wrapper.ServiceBindingDefinition;
import pmsoft.sam.inject.wrapper.WrappingInjectorLoader;
import pmsoft.sam.module.model.SamArchitecture;
import pmsoft.sam.module.model.ServiceKey;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.local.ServiceExecutionEnviromentInternalAPI;
import pmsoft.sam.module.see.transaction.InjectionConfiguration;

public interface InjectionConfigurationInternal extends InjectionConfiguration {

	public ServiceBindingDefinition getBindingDefinition(ServiceExecutionEnviroment see, SamArchitecture architecture);

	public ServiceKey getServiceSpecification();

	public void loadInjectionConfiguration(ServiceExecutionEnviroment see, SamArchitecture architecture,
			WrappingInjectorLoader wrappingInjectorLoader);

	public Injector createTransactionInjector(ServiceExecutionEnviromentInternalAPI seeInternalApi, SamArchitecture architecture, CanonicalProtocolExecutionService canonicalExecutionService, URL transactionURL);

}
