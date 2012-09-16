package pmsoft.sam.module.definition.test;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Rule;
import org.junit.Test;

import pmsoft.sam.model.instance.SIID;
import pmsoft.sam.model.instance.ServiceImplementationKey;
import pmsoft.sam.module.definition.test.data.impl.b.Service1ImplPackB;
import pmsoft.sam.module.definition.test.data.impl.b.Service2ImplPackBalpha;
import pmsoft.sam.module.definition.test.data.impl.b.Service2ImplPackBbeta;
import pmsoft.sam.module.definition.test.data.impl.b.ServiceImplementationPackageB;
import pmsoft.sam.module.definition.test.data.service.Service1Definition;
import pmsoft.sam.module.definition.test.data.service.Service1a;
import pmsoft.sam.module.definition.test.data.service.Service2Definition;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.serviceRegistry.SamServiceRegistry;
import pmsoft.sam.module.see.transaction.InjectionConfiguration;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;
import pmsoft.sam.test.environment.SamProtoTestModule;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class ServiceInteractionTest {
	@Rule
	public GuiceBerryRule guiceBerry = new GuiceBerryRule(SamProtoTestModule.class);

	@Inject
	private SamServiceRegistry serviceRegistry;

	@Inject
	private ServiceExecutionEnviroment see;
	
	@Test
	public void testServiceCreation() {
		// Register implementations
		serviceRegistry.registerServiceImplementation(new ServiceImplementationPackageB());
		ServiceImplementationKey service1 = new ServiceImplementationKey(Service1Definition.class, Service1ImplPackB.class);
		ServiceImplementationKey service2alpha = new ServiceImplementationKey(Service2Definition.class,
				Service2ImplPackBalpha.class);
		ServiceImplementationKey service2beta = new ServiceImplementationKey(Service2Definition.class,
				Service2ImplPackBbeta.class);

		SIID running1 = see.executeServiceInstance(service1);
		SIID running2alpha = see.executeServiceInstance(service2alpha);
		SIID running2beta = see.executeServiceInstance(service2beta);

		// Create alpha transaction and run
		System.out.println("Start single transaction run (alpha)");

		TransactionConfigurator transactionAlpha = see.createTransactionConfiguration(running1);
		InjectionConfiguration configurationAlpha = transactionAlpha.getInjectionConfiguration();
		configurationAlpha.bindInstance(running2alpha);

		SamTransaction realTransactionAlpha = transactionAlpha.createTransactionContext();

		Injector serviceInjector = realTransactionAlpha.getInjector();
		Service1a serviceInterface = serviceInjector.getInstance(Service1a.class);
		assertTrue(serviceInterface.testServiceInteraction());

		// Create beta transaction and run randomly
		System.out.println("Start multiple random transaction run (alpha and beta)");
		TransactionConfigurator transactionBeta = see.createTransactionConfiguration(running1);
		InjectionConfiguration configuration = transactionBeta.getInjectionConfiguration();
		configuration.bindInstance(running2beta);
		SamTransaction realTransactionBeta = transactionBeta.createTransactionContext();

		Random random = new Random();
		Service1a callReference;
		Injector transactionInjector;
		for (int i = 0; i < 10; i++) {
			int select = random.nextInt(2);
			if (select == 0) {
				transactionInjector = realTransactionAlpha.getInjector();
			} else {
				transactionInjector = realTransactionBeta.getInjector();
			}
			callReference= transactionInjector.getInstance(Service1a.class);
			assertTrue(callReference.testServiceInteraction());
		}

	}

}
