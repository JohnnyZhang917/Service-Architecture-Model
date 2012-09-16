package pmsoft.sam.module.canonical;

import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;

import com.google.inject.PrivateModule;

public class CanonicalProtocolInfrastructure extends PrivateModule{

	@Override
	protected void configure() {
		bind(CanonicalProtocolExecutionService.class).to(PrototypeCanonicalProtocolExecutionService.class).asEagerSingleton();
		expose(CanonicalProtocolExecutionService.class);
	}

}
