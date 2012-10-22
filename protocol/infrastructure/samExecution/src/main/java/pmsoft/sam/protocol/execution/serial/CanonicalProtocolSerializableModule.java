package pmsoft.sam.protocol.execution.serial;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceProviderApi;
import pmsoft.sam.protocol.execution.StandardCanonicalProtocolExecutionServiceProviderApi;

import com.google.inject.AbstractModule;

public class CanonicalProtocolSerializableModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CanonicalProtocolExecutionServiceProviderApi.class).to(StandardCanonicalProtocolExecutionServiceProviderApi.class).asEagerSingleton();
		bind(CanonicalProtocolExecutionServiceClientApi.class).to(SerializableClientApi.class);
	}

}
