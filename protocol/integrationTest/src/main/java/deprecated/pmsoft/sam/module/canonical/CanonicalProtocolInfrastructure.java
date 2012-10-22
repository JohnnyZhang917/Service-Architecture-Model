package deprecated.pmsoft.sam.module.canonical;

import com.google.inject.PrivateModule;

public class CanonicalProtocolInfrastructure extends PrivateModule{

	@Override
	protected void configure() {
//		bind(CanonicalProtocolExecutionService.class).to(PrototypeCanonicalProtocolExecutionService.class).asEagerSingleton();
//		expose(CanonicalProtocolExecutionService.class);
	}

}
