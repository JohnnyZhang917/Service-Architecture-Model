package eu.pmsoft.testing;

import com.google.inject.PrivateModule;

public class SimpleConfiguration extends PrivateModule {
    @Override
    protected void configure() {
        expose(ClientApi.class);
        bind(ClientApi.class).to(ClientApiImplementation.class);
        bind(ClientInternalApi.class).to(ClientInternal.class);
    }
}
