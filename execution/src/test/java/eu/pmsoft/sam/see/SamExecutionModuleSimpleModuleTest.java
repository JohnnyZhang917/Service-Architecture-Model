package eu.pmsoft.sam.see;

import com.google.inject.AbstractModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SamExecutionModuleSimpleModuleTest extends AbstractModule {
    @Override
    protected void configure() {
        install(new SamExecutionModuleSimpleModule());
        bind(SamExecutionEnvironmentSimpleTest.class);
        bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
    }
}
