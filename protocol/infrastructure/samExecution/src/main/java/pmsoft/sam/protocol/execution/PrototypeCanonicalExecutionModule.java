package pmsoft.sam.protocol.execution;

import com.google.inject.AbstractModule;

public class PrototypeCanonicalExecutionModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CanonicalProtocolExecutionService.class).to(PrototypeCanonicalProtocolExecutionService.class).asEagerSingleton();
	}

}
