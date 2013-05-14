package eu.pmsoft.sam.injection;


import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class ExtrenalBindingInfrastructureModule extends PrivateModule {

    /**
     * ExternalInstanceProvider is injected to the injection context using the ExternalBindingController interface.
     * The scope ExternalBindingScope implements ExternalBindingController.
     * <p/>
     * Guice requires a provider to be passed, so using this empty one.
     */
    private static final Provider<ExternalInstanceProvider> NOT_USED_PROVIDER = new Provider<ExternalInstanceProvider>() {
        @Override
        public ExternalInstanceProvider get() {
            throw new RuntimeException("use of this provider is not possible");
        }
    };

    @Override
    protected void configure() {
        ExternalBindingScope infrastructureScope = new ExternalBindingScope();
        bind(ExternalBindingController.class).toInstance(infrastructureScope);
        bind(ExternalInstanceProvider.class).toProvider(NOT_USED_PROVIDER).in(infrastructureScope);
        expose(ExternalBindingController.class);
        expose(ExternalInstanceProvider.class);
        binder().requireExplicitBindings();
    }

}
