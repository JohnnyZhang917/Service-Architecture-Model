package eu.pmsoft.sam.see.transport;

import com.google.inject.AbstractModule;

public class SamTransportModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SamTransportLayer.class).to(SamTransportLayerImplementation.class).asEagerSingleton();
    }
}
