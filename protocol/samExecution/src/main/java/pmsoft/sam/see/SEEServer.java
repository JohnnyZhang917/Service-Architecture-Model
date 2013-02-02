package pmsoft.sam.see;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import pmsoft.exceptions.*;
import pmsoft.execution.*;
import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition;
import pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import pmsoft.sam.see.api.SamArchitectureManagement;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.SamServiceDiscovery;
import pmsoft.sam.see.api.SamServiceRegistry;
import pmsoft.sam.see.configuration.SEEConfiguration;
import pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class SEEServer {
    public final static String SERVER_ADDRESS_BIND = "SERVER_ADDRESS_BIND";

	private final Injector serverInjector;
    private final ThreadExecutionServer server;
    private final InetSocketAddress address;
    private final OperationReportingFactory operationReportingFactory;

    /**
	 * Visible for testing
     * @param serverAddressBind
     */
	public static Module createServerModule(final InetSocketAddress serverAddressBind){
		List<Module> serverModules = Lists.newArrayList();
        serverModules.add(new ThreadExecutionModule());
        // TODO extract as configuration
        serverModules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
                bind(InternalLogicContextFactory.class).to(SEELogicContextFactory.class).asEagerSingleton();
            }
        });
		serverModules.add(new LocalSeeExecutionModule());
        serverModules.add(new OperationReportingModule());
        serverModules.add(new LocalSeeInfrastructureModule());
        //TODO setup this some where else, without inner class
        serverModules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(InetSocketAddress.class).annotatedWith(Names.named(SEEServer.SERVER_ADDRESS_BIND)).toInstance(serverAddressBind);
            }
        });
		return Modules.combine(serverModules);
	}

	public SEEServer(final SEEConfiguration configuration) throws OperationCheckedException {
        ErrorsReport initializationContext = new ErrorsReport();
        this.address = configuration.address;
		Module pluginsModule = Modules.combine(configuration.pluginModules);
		serverInjector = Guice.createInjector(createServerModule(configuration.address),pluginsModule);
        ThreadExecutionInfrastructure infrastructure = serverInjector.getInstance(ThreadExecutionInfrastructure.class);
        this.server = infrastructure.createServer(address);
        this.operationReportingFactory = serverInjector.getInstance(OperationReportingFactory.class);
        initializeServerNode(configuration);
    }

    private void initializeServerNode(SEEConfiguration configuration) throws OperationCheckedException {
        OperationContext operationContext = operationReportingFactory.ensureEmptyContext();
        try {
            SamArchitectureManagement architectureManager = serverInjector.getInstance(SamArchitectureManagement.class);
            SamExecutionNode executionNode = serverInjector.getInstance(SamExecutionNode.class);
            SamServiceRegistry samServiceRegistry = serverInjector.getInstance(SamServiceRegistry.class);

            for (SamArchitectureDefinition architectureDef : configuration.architectures) {
                SamArchitecture architecture;
                try {
                    architecture = ArchitectureModelLoader.loadArchitectureModel(architectureDef);
                    architectureManager.registerArchitecture(architecture);
                } catch (IncorrectArchitectureDefinition e) {
                    operationContext.getErrors().addError(e, "error on load of architecture %s", architectureDef);
                }
            }
            for (SamServiceImplementationPackageContract implPackage : configuration.implementationPackages) {
                samServiceRegistry.registerServiceImplementationPackage(implPackage);
            }
            for (SEEServiceSetupAction setup : configuration.setupActions) {
                setup.setupService(executionNode);
            }
        } catch (OperationRuntimeException operationError) {
            operationContext.getErrors().addError(operationError);
        } finally {
            operationReportingFactory.closeContext(operationContext);
            operationContext.throwOnErrors();
        }
    }

    public void executeSetupAction(SEEServiceSetupAction setup) throws OperationCheckedException {
        OperationContext operationContext = operationReportingFactory.openNestedContext();
        try {
            SamExecutionNode executionNode = serverInjector.getInstance(SamExecutionNode.class);
            setup.setupService(executionNode);
        } catch (OperationRuntimeException operationError) {
            operationContext.getErrors().addError(operationError);
        } finally {
            operationReportingFactory.closeContext(operationContext);
            operationContext.throwOnErrors();
        }
	}

	public SamServiceDiscovery getServiceDiscovery() {
		return serverInjector.getInstance(SamServiceDiscovery.class);
	}

	public void startUpServer() {
        server.startServer();
	}

	public void shutdownServer() {
        server.shutdownServer();
	}

    public <R,T> Future<R> executeServiceAction(ServiceAction<R,T> interaction) {
        return server.executeServiceAction(interaction);
    }
}
