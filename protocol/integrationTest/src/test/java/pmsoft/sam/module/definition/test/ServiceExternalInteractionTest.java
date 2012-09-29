package pmsoft.sam.module.definition.test;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Rule;
import org.junit.Test;

import pmsoft.sam.module.definition.test.data.impl.b.Service1ImplPackB;
import pmsoft.sam.module.definition.test.data.impl.b.Service2ImplPackBalpha;
import pmsoft.sam.module.definition.test.data.impl.b.ServiceImplementationPackageB;
import pmsoft.sam.module.definition.test.data.service.Service1Definition;
import pmsoft.sam.module.definition.test.data.service.Service1a;
import pmsoft.sam.module.definition.test.data.service.Service2Definition;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.serviceRegistry.SamServiceRegistryDeprecated;
import pmsoft.sam.module.see.transaction.InjectionConfiguration;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.ServiceImplementationKey;
import pmsoft.sam.test.environment.SamProtoTestModule;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class ServiceExternalInteractionTest {
	@Rule
	public GuiceBerryRule guiceBerry = new GuiceBerryRule(SamProtoTestModule.class);

	@Inject
	private SamServiceRegistryDeprecated serviceRegistry;

	@Inject
	private ServiceExecutionEnviroment see;

	@Test
	public void testServiceCreation() {
		// Register implementations
		serviceRegistry.registerServiceImplementation(new ServiceImplementationPackageB());
		ServiceImplementationKey service1 = new ServiceImplementationKey(Service1Definition.class, Service1ImplPackB.class);
		ServiceImplementationKey service2 = new ServiceImplementationKey(Service2Definition.class, Service2ImplPackBalpha.class);

		SIID running1 = see.executeServiceInstance(service1);
		SIID running2 = see.executeServiceInstance(service2);

		// Create a transaction wrapping service2
		TransactionConfigurator transactionService2Configurator = see.createTransactionConfiguration(running2);
		SamTransaction service2Transaction = transactionService2Configurator.createTransactionContext();
		URL service2TransactionURL = service2Transaction.getTransactionURL();

		// Create a transaction for service1 and bind service2 as external
		// instance
		TransactionConfigurator transactionService1Configurator = see.createTransactionConfiguration(running1);
		InjectionConfiguration configurationAlpha = transactionService1Configurator.getInjectionConfiguration();
		configurationAlpha.bindExternalInstance(service2.getServiceSpecificationKey(), service2TransactionURL);

		SamTransaction realTransactionAlpha = transactionService1Configurator.createTransactionContext();
		// Test service interaction
		Injector serviceInjector = realTransactionAlpha.getInjector();
		Service1a serviceInterface = serviceInjector.getInstance(Service1a.class);
		assertTrue(serviceInterface.testServiceInteraction());
	}

}
