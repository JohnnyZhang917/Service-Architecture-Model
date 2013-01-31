package pmsoft.sam.exceptions;

import com.google.inject.AbstractModule;

public class SamOperationContextModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SamOperationContextFactory.class).asEagerSingleton();
    }
}
