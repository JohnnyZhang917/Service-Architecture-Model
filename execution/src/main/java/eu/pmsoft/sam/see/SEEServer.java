package eu.pmsoft.sam.see;

import com.google.inject.Singleton;

public class SEEServer {
//
//    private final ThreadExecutionServer server;
//    private final ThreadExecutionManager executionManager;
//    private final OperationReportingFactory operationReportingFactory;
//    private final SamArchitectureManagement architectureManager;
//    private final SamExecutionNode executionNode;
//    private final SamServiceRegistryDeprecated samServiceRegistryDeprecated;
//    private final SamServiceDiscovery serviceDiscovery;
//
//    @Inject
//    public SEEServer(ThreadExecutionServer server, ThreadExecutionManager executionManager, OperationReportingFactory operationReportingFactory,
//                     SEEConfiguration configuration, SamArchitectureManagement architectureManager,
//                     SamExecutionNode executionNode, SamServiceRegistryDeprecated samServiceRegistryDeprecated,
//                     SamServiceDiscovery serviceDiscovery) throws OperationCheckedException {
//        this.server = server;
//        this.executionManager = executionManager;
//        this.operationReportingFactory = operationReportingFactory;
//        this.architectureManager = architectureManager;
//        this.executionNode = executionNode;
//        this.samServiceRegistryDeprecated = samServiceRegistryDeprecated;
//        this.serviceDiscovery = serviceDiscovery;
//        initializeServerNode(configuration);
//    }
//
//    public static SEEServer createServer(final SEEConfiguration configuration) {
//        Injector injector = Guice.createInjector(createServerModule(configuration));
//        return injector.getInstance(SEEServer.class);
//    }
//
//
//    /**
//     * Visible for testing
//     *
//     * @param configuration
//     */
//    public static Module createServerModule(final SEEConfiguration configuration) {
//        List<Module> serverModules = Lists.newArrayList();
////        serverModules.add(new LoggerInjectorModule());
//////        serverModules.addAll(configuration.pluginModules);
////        serverModules.add(new ThreadExecutionModule());
////        // TODO extract as configuration
////        serverModules.add(new AbstractModule() {
////            @Override
////            protected void configure() {
////                bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
////                bind(ThreadExecutionLogicProvider.class).to(SEELogicContextLogic.class).asEagerSingleton();
////            }
////        });
////        serverModules.add(new LocalSeeExecutionModule());
////        serverModules.add(new SamTransportModule());
////        serverModules.add(new OperationReportingModule());
////        serverModules.add(new LocalSeeInfrastructureModule());
////        //TODO setup this some where else, without inner class
////        // TODO FIXME, dont inject address. Should be passed during initialization
////        serverModules.add(new AbstractModule() {
////            @Override
////            protected void configure() {
////                bind(SEEServer.class);
////                bind(SEEConfiguration.class).toInstance(configuration);
//////                bind(InetSocketAddress.class).toInstance(configuration.address);
////            }
////        });
//        return Modules.combine(serverModules);
//    }
//
//    private void initializeServerNode(SEEConfiguration configuration) throws OperationCheckedException {
//        OperationContext operationContext = operationReportingFactory.ensureEmptyContext();
//        try {
//            for (SamArchitectureDefinition architectureDef : configuration.architectures) {
//                SamArchitecture architecture;
//                try {
//                    architecture = ArchitectureModelLoader.loadArchitectureModel(architectureDef);
//                    architectureManager.registerArchitecture(architecture);
//                } catch (IncorrectArchitectureDefinition e) {
//                    operationContext.getErrors().addError(e, "error on load of architecture %s", architectureDef);
//                }
//            }
//            for (SamServiceImplementationPackageContract implPackage : configuration.implementationPackages) {
//                samServiceRegistryDeprecated.registerServiceImplementationPackage(implPackage);
//            }
////            for (SEEServiceSetupAction setup : configuration.setupActions) {
////                setup.setupService(executionNode);
////            }
//        } catch (OperationRuntimeException operationError) {
//            operationContext.getErrors().addError(operationError);
//        } finally {
//            operationReportingFactory.closeContext(operationContext);
//            operationContext.throwOnErrors();
//        }
//    }
//
//    public void executeSetupAction(SEEServiceSetupAction setup) throws OperationCheckedException {
//        OperationContext operationContext = operationReportingFactory.openNestedContext();
//        try {
//            setup.setupService(null);
////            setup.setupService(executionNode);
//        } catch (OperationRuntimeException operationError) {
//            operationContext.getErrors().addError(operationError);
//        } finally {
//            operationReportingFactory.closeContext(operationContext);
//            operationContext.throwOnErrors();
//        }
//    }
//
//    public SamServiceDiscovery getServiceDiscovery() {
//        return this.serviceDiscovery;
//    }
//
//    public void startUpEnvironment() {
//        server.startServer();
//    }
//
//    public void shutdownEnvironment() {
//        server.shutdownServer();
//    }
//
////    public <R, T> Future<R> executeServiceAction(ServiceAction<R, T> interaction) {
////        return executionManager.executeServiceAction(interaction);
////    }
}
