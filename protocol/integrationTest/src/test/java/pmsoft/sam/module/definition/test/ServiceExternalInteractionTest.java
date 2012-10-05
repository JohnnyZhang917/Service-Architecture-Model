package pmsoft.sam.module.definition.test;


public class ServiceExternalInteractionTest {
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
//	public void testServiceCreation() {
//		// Register implementations
//		serviceRegistry.registerServiceImplementation(new ServiceImplementationPackageB());
//		ServiceImplementationKey service1 = new ServiceImplementationKey(Service1Definition.class, Service1ImplPackB.class);
//		ServiceImplementationKey service2 = new ServiceImplementationKey(Service2Definition.class, Service2ImplPackBalpha.class);
//
//		SIID running1 = see.executeServiceInstance(service1);
//		SIID running2 = see.executeServiceInstance(service2);
//
//		// Create a transaction wrapping service2
//		TransactionConfigurator transactionService2Configurator = see.createTransactionConfiguration(running2);
//		SamTransaction service2Transaction = transactionService2Configurator.createTransactionContext();
//		URL service2TransactionURL = service2Transaction.getTransactionURL();
//
//		// Create a transaction for service1 and bind service2 as external
//		// instance
//		TransactionConfigurator transactionService1Configurator = see.createTransactionConfiguration(running1);
//		InjectionConfiguration configurationAlpha = transactionService1Configurator.getInjectionConfiguration();
//		configurationAlpha.bindExternalInstance(service2.getServiceSpecificationKey(), service2TransactionURL);
//
//		SamTransaction realTransactionAlpha = transactionService1Configurator.createTransactionContext();
//		// Test service interaction
//		Injector serviceInjector = realTransactionAlpha.getInjector();
//		Service1a serviceInterface = serviceInjector.getInstance(Service1a.class);
//		assertTrue(serviceInterface.testServiceInteraction());
//	}
//
}
