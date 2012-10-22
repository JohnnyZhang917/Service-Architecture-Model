package pmsoft.sam.see.api.data;

import pmsoft.sam.see.SEEConfiguration;
import pmsoft.sam.see.SEEConfigurationBuilder;
import pmsoft.sam.see.SEEServiceSetupAction;
import pmsoft.sam.see.api.data.architecture.SeeTestArchitecture;
import pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import pmsoft.sam.see.api.data.transactions.TestTransactionDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInstanceTransaction;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

public class TestServiceExecutionEnvironmentConfiguration {

	public static SEEConfiguration createTestConfiguration() {
		return SEEConfigurationBuilder.configuration().architecture(new SeeTestArchitecture()).implementationPackage(new TestImplementationDeclaration())
				.setupAction(new SEEServiceSetupAction() {

					@Override
					public void setup() {
						SIID one = createServiceInstance(TestServiceOneModule.class);
						SIID two = createServiceInstance(TestServiceTwoModule.class);

						SamInjectionConfiguration oneSingle = TestTransactionDefinition.createServiceOneConfiguration(one);
						SamInstanceTransaction transactionOne = setupServiceTransaction(oneSingle);
						SIURL oneUrl = transactionOne.getTransactionURL();

						SamInjectionConfiguration twoLocal = TestTransactionDefinition.createServiceTwoConfiguration(two, one);
						SamInstanceTransaction transactionTwo = setupServiceTransaction(twoLocal);
						SIURL twoUrl = transactionTwo.getTransactionURL();

					}
				}).bindToPort(4999);
	}

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
