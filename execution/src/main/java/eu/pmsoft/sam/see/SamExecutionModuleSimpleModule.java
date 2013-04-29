package eu.pmsoft.sam.see;

import com.google.inject.AbstractModule;
import eu.pmsoft.injectionUtils.logger.LoggerInjectorModule;
import eu.pmsoft.sam.see.api.SamExecutionEnvironment;
import eu.pmsoft.sam.see.api.infrastructure.SamExecutionEnvironmentInfrastructure;
import eu.pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import eu.pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;
import eu.pmsoft.sam.see.transport.netty.SamTransportNettyModule;

public class SamExecutionModuleSimpleModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new LoggerInjectorModule());
        install(new LocalSeeExecutionModule());
        install(new SamTransportNettyModule());
        install(new LocalSeeInfrastructureModule());

        bind(SamExecutionEnvironment.class).to(SamExecutionEnvironmentSimple.class).asEagerSingleton();
        bind(SamExecutionEnvironmentInfrastructure.class).to(SamExecutionEnvironmentInfrastructureSimple.class).asEagerSingleton();

//        serverModules.add();
//        serverModules.addAll(configuration.pluginModules);
//        serverModules.add(new ThreadExecutionModule());
//        // TODO extract as configuration
//        serverModules.add(new AbstractModule() {
//            @Override
//            protected void configure() {
//                bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
//                bind(ThreadExecutionLogicProvider.class).to(SEELogicContextLogic.class).asEagerSingleton();
//            }
//        });
//        serverModules.add();
//        serverModules.add(new SamTransportModule());
//        serverModules.add(new OperationReportingModule());
//        serverModules.add(new LocalSeeInfrastructureModule());
    }
}
