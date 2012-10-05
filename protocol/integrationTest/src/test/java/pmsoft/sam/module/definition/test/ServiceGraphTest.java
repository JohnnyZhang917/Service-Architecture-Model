package pmsoft.sam.module.definition.test;


public class ServiceGraphTest {
//	@Rule
//	public GuiceBerryRule guiceBerry = new GuiceBerryRule(SamProtoTestModule.class);
//
//	@Inject
//	private SamServiceRegistryDeprecated serviceRegistry;
//
//	@Inject
//	private ServiceExecutionEnviroment see;
//	
//	@Test
//	public void testServiceGraphCreation() {
//		// Register implementations
//		serviceRegistry.registerServiceImplementation(new ServiceImplementationPackageB());
//		ServiceImplementationKey service1 = new ServiceImplementationKey(Service1Definition.class, Service1ImplPackB.class);
//		ServiceImplementationKey service2alpha = new ServiceImplementationKey(Service2Definition.class,
//				Service2ImplPackBalpha.class);
//		ServiceImplementationKey service2beta = new ServiceImplementationKey(Service2Definition.class,
//				Service2ImplPackBbeta.class);
//
//		SIID running1 = see.executeServiceInstance(service1);
//		SIID running2alpha = see.executeServiceInstance(service2alpha);
//		SIID running2beta = see.executeServiceInstance(service2beta);
//
//		// Create alpha transaction and run
//		System.out.println("Start single transaction run (alpha)");
//
//		TransactionConfigurator transactionAlpha = see.createTransactionConfiguration(running1);
//		InjectionConfiguration configurationAlpha = transactionAlpha.getInjectionConfiguration();
//		configurationAlpha.bindInstance(running2alpha);
//
//		SamTransaction realTransactionAlpha = transactionAlpha.createTransactionContext();
//
//		Injector serviceInjectorAlpha = realTransactionAlpha.getInjector();
//
//		
//		// Create beta transaction and run randomly
//		System.out.println("Start multiple random transaction run (alpha and beta)");
//		TransactionConfigurator transactionBeta = see.createTransactionConfiguration(running1);
//		InjectionConfiguration configuration = transactionBeta.getInjectionConfiguration();
//		configuration.bindInstance(running2beta);
//		SamTransaction realTransactionBeta = transactionBeta.createTransactionContext();
//
//		Injector serviceInjectorBeta = realTransactionBeta.getInjector();
//
//		ServiceInstance runningInstance = see.getServiceInstance(running1);
//		
//		Grapher.graphGood("/tmp/running.dot", runningInstance.getInjector());
//		Grapher.graphGood("/tmp/alpha.dot", serviceInjectorAlpha);
//		Grapher.graphGood("/tmp/beta.dot", serviceInjectorBeta);
//	}
//
}
