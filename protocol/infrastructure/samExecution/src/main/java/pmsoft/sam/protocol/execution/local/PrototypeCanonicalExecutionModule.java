package pmsoft.sam.protocol.execution.local;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceProviderApi;
import pmsoft.sam.protocol.execution.ServiceExecutionEnvironment;
import pmsoft.sam.protocol.execution.StandardCanonicalProtocolExecutionServiceProviderApi;

import com.google.inject.AbstractModule;

public class PrototypeCanonicalExecutionModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CanonicalProtocolExecutionServiceClientApi.class).to(PrototypeCanonicalProtocolExecutionServiceClientApi.class).asEagerSingleton();
		bind(CanonicalProtocolExecutionServiceProviderApi.class).to(StandardCanonicalProtocolExecutionServiceProviderApi.class).asEagerSingleton();
		bind(ServiceExecutionEnvironment.class).to(LocalServiceExecutionEnvironment.class).asEagerSingleton();
	}

}
