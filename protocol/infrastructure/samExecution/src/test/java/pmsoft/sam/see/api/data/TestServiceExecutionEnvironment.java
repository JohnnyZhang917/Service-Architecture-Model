package pmsoft.sam.see.api.data;

public class TestServiceExecutionEnvironment {
	//
	// private SIURL oneUrl;
	// private SIURL twoUrl;
	//
	// @Inject
	// public TestServiceExecutionEnvironment(SamArchitectureManagement
	// architectureManager, SamServiceRegistry samServiceRegistry,
	// SamExecutionNode executionNode, ServiceExecutionInfrastructure
	// infrastructure) {
	// super(architectureManager, samServiceRegistry, executionNode,
	// infrastructure);
	// }
	//
	// @Override
	// public void configure() {
	// registerArchitecture(new SeeTestArchitecture());
	// registerImplementationPackage(new TestImplementationDeclaration());
	//
	// SIID one = createServiceInstance(TestServiceOneModule.class);
	// SIID two = createServiceInstance(TestServiceTwoModule.class);
	//
	// SamInjectionConfiguration oneSingle =
	// TestTransactionDefinition.createServiceOneConfiguration(one);
	// SamInstanceTransaction transactionOne =
	// setupServiceTransaction(oneSingle);
	// oneUrl = transactionOne.getTransactionURL();
	//
	// SamInjectionConfiguration twoLocal =
	// TestTransactionDefinition.createServiceTwoConfiguration(two, one);
	// SamInstanceTransaction transactionTwo =
	// setupServiceTransaction(twoLocal);
	// twoUrl = transactionTwo.getTransactionURL();
	// }
	//
	// @Override
	// public boolean executeTestInteraction() {
	// callServiceOne();
	// callServiceTwo();
	// return true;
	// }
	//
	// private void callServiceTwo() {
	// assertNotNull(twoUrl);
	// Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
	// Key<TestInterfaceTwo0> interfaceTwoKey =
	// Key.get(TestInterfaceTwo0.class);
	// CanonicalProtocolExecutionContext executionContext =
	// executionNode.createTransactionExecutionContext(twoUrl);
	// Injector injector = executionContext.getInjector();
	//
	// assertNotNull(injector);
	// assertNotNull(injector.getExistingBinding(interfaceTwoKey));
	// assertNull(injector.getExistingBinding(interfaceOneKey));
	//
	// TransactionController transactionController =
	// executionContext.getTransactionController();
	// transactionController.enterTransactionContext();
	// TestInterfaceTwo0 instanceTwo = injector.getInstance(interfaceTwoKey);
	// assertTrue(instanceTwo.runTest());
	// transactionController.exitTransactionContext();
	// }
	//
	// private void callServiceOne() {
	// CanonicalProtocolExecutionContext executionContext =
	// executionNode.createTransactionExecutionContext(oneUrl);
	// Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
	// Injector injector = executionContext.getInjector();
	// assertNotNull(injector);
	// assertNotNull(injector.getExistingBinding(interfaceOneKey));
	//
	// TestInterfaceOne instanceOne = injector.getInstance(interfaceOneKey);
	// assertTrue(instanceOne.runTest());
	// }

}
