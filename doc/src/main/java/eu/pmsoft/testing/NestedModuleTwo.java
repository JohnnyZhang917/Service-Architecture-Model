package eu.pmsoft.testing;

import com.google.inject.PrivateModule;

public class NestedModuleTwo extends PrivateModule {
    @Override
    protected void configure() {
        expose(LibraryTwoApi.class);
        bind(LibraryTwoApi.class).to(LibraryTwoInternal.class);
        bind(CommonLogApi.class).toInstance(new CommonLogApi() {
            @Override
            public void log(String message) {
                System.out.println("Library TWO log:" + message);
            }
        });
    }
}
