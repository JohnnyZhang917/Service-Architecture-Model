package pmsoft.sam.module.see.local.transaction;

import java.net.URL;

import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.canonical.deprecated.service.CanonicalProtocolExecutionService;
import pmsoft.sam.inject.wrapper.deprecated.ServiceBindingDefinition;
import pmsoft.sam.inject.wrapper.deprecated.WrappingInjectorLoader;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.local.ServiceExecutionEnviromentInternalAPI;
import pmsoft.sam.module.see.transaction.InjectionConfiguration;

import com.google.inject.Injector;

public interface InjectionConfigurationInternal extends InjectionConfiguration {

	public ServiceBindingDefinition getBindingDefinition(ServiceExecutionEnviroment see, SamArchitecture architecture);

	public ServiceKey getServiceSpecification();

	public void loadInjectionConfiguration(ServiceExecutionEnviroment see, SamArchitecture architecture,
			WrappingInjectorLoader wrappingInjectorLoader);

	public Injector createTransactionInjector(ServiceExecutionEnviromentInternalAPI seeInternalApi, SamArchitecture architecture, CanonicalProtocolExecutionService canonicalExecutionService, URL transactionURL);

}
