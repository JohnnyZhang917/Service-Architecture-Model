package eu.pmsoft.sam.injection;

import com.google.inject.*;

public class FreeBindingInjectionUtil {

    public final static <T> void createIntermediateProvider(PrivateBinder binder, final Key<T> key, final int slotNr) {
        Provider<T> provider = new Provider<T>() {
            @Inject
            private Injector injector;
            private volatile int instanceNr = 0;

            public T get() {
                ExternalBindingSwitch<T> intermediate = new ExternalBindingSwitch<T>(key, instanceNr++, slotNr);
                injector.injectMembers(intermediate);
                return intermediate.getReferenceObject();
            }
        };
        binder.bind(key).toProvider(provider);
        binder.expose(key);
    }

    public final static <T> void createGlueBinding(Binder binder, Key<T> key, Injector realInjector) {
        binder.bind(key).toProvider(realInjector.getProvider(key));
    }


}
