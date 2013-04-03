package pmsoft.exceptions;

import com.google.inject.AbstractModule;

public class OperationReportingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(OperationReportingFactory.class).asEagerSingleton();
    }
}
