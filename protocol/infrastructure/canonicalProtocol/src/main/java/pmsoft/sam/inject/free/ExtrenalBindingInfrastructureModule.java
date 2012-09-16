package pmsoft.sam.inject.free;

import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class ExtrenalBindingInfrastructureModule extends PrivateModule {


	@Override
	protected void configure() {
		ExtrenalBindingScope infrastructureScope = new ExtrenalBindingScope();
		bind(ExternalBindingController.class).toInstance(infrastructureScope);
		Provider<ExternalInstanceProvider> seededKeyProvider = infrastructureScope.seededKeyProvider();
		bind(ExternalInstanceProvider.class).toProvider(seededKeyProvider).in(infrastructureScope);
		expose(ExternalBindingController.class);
		expose(ExternalInstanceProvider.class);
		binder().requireExplicitBindings();
	}

}
