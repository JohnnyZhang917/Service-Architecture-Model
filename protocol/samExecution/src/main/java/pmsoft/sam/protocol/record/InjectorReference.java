package pmsoft.sam.protocol.record;

import com.google.inject.Injector;

final class InjectorReference {

    private final Injector injector;

    InjectorReference(Injector injector) {
        this.injector = injector;
    }

    public Injector getInjector() {
        return injector;
    }
}
