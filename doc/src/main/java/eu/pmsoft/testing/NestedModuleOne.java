package eu.pmsoft.testing;

import com.google.inject.PrivateModule;

public class NestedModuleOne extends PrivateModule {
    @Override
    protected void configure() {
        expose(LibraryOneApi.class);
        bind(LibraryOneApi.class).to(LibraryOneInternal.class);
        bind(CommonLogApi.class).toInstance(new CommonLogApi() {
            @Override
            public void log(String message) {
                System.out.println("Library ONE log:" + message);
            }
        });
    }
}
