package pmsoft.sam.protocol.freebinding;


import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class ExtrenalBindingInfrastructureModule extends PrivateModule {

	/**
	 * ExternalInstanceProvider is injected to the injection context using the ExternalBindingController interface.
	 * The scope ExternalBindingScope implements ExternalBindingController.
	 * 
	 * Guice requires a provider to be passed, so using this empty one.
	 */
	private static final Provider<ExternalInstanceProvider> NOT_USED_PROVIDER = new Provider<ExternalInstanceProvider>(){
		@Override
		public ExternalInstanceProvider get() {
			return null;
		}
	};
	
	@Override
	protected void configure() {
		ExtrenalBindingScope infrastructureScope = new ExtrenalBindingScope();
		bind(ExternalBindingController.class).toInstance(infrastructureScope);
		bind(ExternalInstanceProvider.class).toProvider(NOT_USED_PROVIDER).in(infrastructureScope);
		expose(ExternalBindingController.class);
		expose(ExternalInstanceProvider.class);
		binder().requireExplicitBindings();
	}

}
