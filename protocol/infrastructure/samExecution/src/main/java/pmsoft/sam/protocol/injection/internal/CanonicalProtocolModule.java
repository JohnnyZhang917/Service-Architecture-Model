package pmsoft.sam.protocol.injection.internal;

import pmsoft.sam.protocol.injection.CanonicalProtocolInfrastructure;

import com.google.inject.PrivateModule;

public class CanonicalProtocolModule extends PrivateModule {

	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		bind(CanonicalProtocolInfrastructure.class).to(CanonicalProtocolModel.class).asEagerSingleton();
		expose(CanonicalProtocolInfrastructure.class);
	}

}
