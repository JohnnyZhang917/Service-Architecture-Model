package pmsoft.sam.module.definition.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Rule;
import org.junit.Test;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.module.definition.test.data.impl.c.Service3ImplPackC;
import pmsoft.sam.module.definition.test.data.impl.c.Service4ImplPackC;
import pmsoft.sam.module.definition.test.data.impl.c.Service4ImplPackCPrototype;
import pmsoft.sam.module.definition.test.data.impl.c.ServiceImplementationPackageC;
import pmsoft.sam.module.definition.test.data.service.Service3Definition;
import pmsoft.sam.module.definition.test.data.service.Service4Definition;
import pmsoft.sam.module.definition.test.data.service.Service4a;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.serviceRegistry.SamServiceRegistryDeprecated;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.ServiceImplementationKey;
import pmsoft.sam.test.environment.SamProtoTestModule;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

public class NestedExternalInteractionTest {
	@Rule
	public GuiceBerryRule guiceBerry = new GuiceBerryRule(SamProtoTestModule.class);

	@Inject
	private SamServiceRegistryDeprecated serviceRegistry;

	@Inject
	private ServiceExecutionEnviroment see;

	@Test
	public void testServiceCreation() {
		// Register implementations
		serviceRegistry.registerServiceImplementation(new ServiceImplementationPackageC());
		ServiceImplementationKey service3 = new ServiceImplementationKey(Service3Definition.class, Service3ImplPackC.class);
		ServiceImplementationKey service4 = new ServiceImplementationKey(Service4Definition.class, Service4ImplPackC.class);
		ServiceImplementationKey service4Prototype = new ServiceImplementationKey(Service4Definition.class, Service4ImplPackCPrototype.class);

		// This service implementation dont have any external dependency
		SIID running4Prototype = see.executeServiceInstance(service4Prototype);
		URL running4PrototypeURL = createSingleServiceTransaction(running4Prototype);
		
		int nestedLevel = 5;
		SIID[] running3Service = new SIID[nestedLevel];
		SIID[] running4Service = new SIID[nestedLevel];
		URL current3URL = null;
		URL current4URL = running4PrototypeURL;
		for (int i = 0; i < nestedLevel; i++) {
			running3Service[i] = see.executeServiceInstance(service3);
			running4Service[i] = see.executeServiceInstance(service4);
			
			current3URL = createNestedServiceTransaction(running3Service[i],service4.getServiceSpecificationKey(),current4URL);
			current4URL = createNestedServiceTransaction(running4Service[i],service3.getServiceSpecificationKey(),current3URL);
		}
		
		SamTransaction headTransaction =see.getSamTransaction(current4URL);
		Service4a headServiceInstance = headTransaction.getInjector().getInstance(Service4a.class);
		
		assertTrue(headServiceInstance.executeNextNestedService(5));
		assertTrue(headServiceInstance.executeNextNestedService(10));
		assertFalse(headServiceInstance.executeNextNestedService(11));

	}

	private URL createNestedServiceTransaction(SIID siid, ServiceKey serviceSpecificationKey, URL url) {
		TransactionConfigurator configurator = see.createTransactionConfiguration(siid);
		configurator.getInjectionConfiguration().bindExternalInstance(serviceSpecificationKey, url);
		SamTransaction transaction = configurator.createTransactionContext();
		URL transactionURL = transaction.getTransactionURL();
		return transactionURL;
	}

	private URL createSingleServiceTransaction(SIID siid) {
		TransactionConfigurator configurator = see.createTransactionConfiguration(siid);
		SamTransaction transaction = configurator.createTransactionContext();
		URL transactionURL = transaction.getTransactionURL();
		return transactionURL;
	}

}
