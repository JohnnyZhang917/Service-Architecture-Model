package pmsoft.sam.see.api;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.net.MalformedURLException;
import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import pmsoft.sam.architecture.api.SamArchitectureManagement;
import pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import pmsoft.sam.architecture.loader.IncorrectArchitectureDefinition;
import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.data.architecture.SeeTestArchitecture;
import pmsoft.sam.see.api.data.architecture.TestInterfaceOne;
import pmsoft.sam.see.api.data.architecture.TestInterfaceTwo0;
import pmsoft.sam.see.api.data.architecture.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.TestServiceTwo;
import pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import pmsoft.sam.see.api.data.transactions.TestTransactionDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamServiceImplementation;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.api.transaction.SamInjectionTransaction;
import pmsoft.sam.see.api.transaction.SamInjectionTransactionConfiguration;
import pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

@Guice(modules = {LocalSeeExecutionModule.class,LocalSeeInfrastructureModule.class})
public class TestServiceExecutionEnvironment {
	
//	@Inject
//	private SamArchitectureRegistry architectureRegistry;
	@Inject
	private SamArchitectureManagement architectureManager;
	
	@Inject
	private SamServiceRegistry samServiceRegistry;
	@Inject
	private SamExecutionNode executionNode;
	

	@DataProvider(name = "architecturesToSetup")
	public Object[][] listOfArchitectures() throws IncorrectArchitectureDefinition {
		return new Object[][] { { ArchitectureModelLoader.loadArchitectureModel(new SeeTestArchitecture()) } };
	}
	
	@DataProvider(name = "implementationDeclarations")
	public Object[][] listOfImplementationDeclarations() {
		return new Object[][] { { new TestImplementationDeclaration() } };
	}

	@DataProvider(name = "registeredImplementations")
	public Object[][] listOfProvidedImplementationKeys() {
		return new Object[][] { { new SamServiceImplementationKey(TestServiceOneModule.class) }, { new SamServiceImplementationKey(TestServiceTwoModule.class) } };
	}

	@Test(dataProvider = "architecturesToSetup", groups = "architectureSetup")
	public void testLoadOfArchitectureInformation(SamArchitecture architecture) {
		architectureManager.registerArchitecture(architecture);
	}

	
	@Test(dataProvider = "implementationDeclarations", groups = "architectureDefinition", dependsOnGroups="architectureSetup")
	public void testRegistrationOfImplementationPackage(TestImplementationDeclaration declaration) {
		samServiceRegistry.registerServiceImplementationPackage(declaration);
	}
	
	@Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck", dependsOnGroups="architectureDefinition")
	public void testLoadOfImplementations(SamServiceImplementationKey serviceKey) {
		SamServiceImplementation registered = samServiceRegistry.getImplementation(serviceKey);
		assertNotNull(registered);
	}

	@Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck", dependsOnGroups="architectureDefinition")
	public void testServiceInstanceCreation(SamServiceImplementationKey key) {
		SamServiceInstance serviceInstance = executionNode.createServiceInstance(key,null);
		assertNotNull(serviceInstance);
		assertNotNull(serviceInstance.getInjector());
		assertNotNull(serviceInstance.getKey());
	}
	
	private SIID getUniqueServiceInstance(Class<? extends Module> implementationModule ) {
		SamServiceImplementationKey serviceKey = new SamServiceImplementationKey(implementationModule);

		Set<SamServiceInstance> setInstance = executionNode.searchInstance(serviceKey, null);

		assertEquals("1 instance of Service expected",1, setInstance.size());
		SamServiceInstance instance = setInstance.iterator().next();
		return instance.getKey();
	}

	@Test( groups = "transactionsCreation", dependsOnGroups="architectureLoadCheck")
	public void testInjectionTransactionCreation() throws MalformedURLException {
		SIID siidOne = getUniqueServiceInstance(TestServiceOneModule.class);
		SIID siidTwo = getUniqueServiceInstance(TestServiceTwoModule.class);

		ServiceKey serviceOneTypeKey = new ServiceKey(TestServiceOne.class);
		ServiceKey serviceTwoTypeKey = new ServiceKey(TestServiceTwo.class);
		
		SamInjectionTransactionConfiguration transactionOne = TestTransactionDefinition.createServiceOneTransaction(siidOne);
		assertNotNull(transactionOne);
		assertEquals(siidOne, transactionOne.getExposedServiceInstance());
		assertEquals(serviceOneTypeKey, transactionOne.getProvidedService());
		
		SamInjectionTransactionConfiguration transactionTwo = TestTransactionDefinition.createServiceTwoTransaction(siidTwo, siidOne);
		assertNotNull(transactionTwo);
		assertEquals(siidTwo, transactionTwo.getExposedServiceInstance());
		assertEquals(siidOne, transactionTwo.getInternalInjectionConfiguration().get(serviceOneTypeKey));
		assertEquals(serviceTwoTypeKey, transactionTwo.getProvidedService());
		
		checkTransactionRegistration(new SIURL("http://localhost/one"),transactionOne);
		checkTransactionRegistration(new SIURL("http://localhost/two"),transactionTwo);
	}

	private void checkTransactionRegistration(SIURL url, SamInjectionTransactionConfiguration transaction) {
		executionNode.setupInjectionTransaction(transaction, null, url);
		SamInjectionTransaction transactionRegistered = executionNode.getTransaction(url);
		assertNotNull(transactionRegistered);
		SamInjectionTransaction transanctionOnRegistry = samServiceRegistry.getTransaction(url);
		assertNotNull(transanctionOnRegistry);
		assertEquals(transactionRegistered, transanctionOnRegistry);		
	}

	@Test( groups = "transactionsExecution", dependsOnGroups="transactionsCreation")
	public void testInjectionTransactionExecutionForServiceOne() throws MalformedURLException {
		SIURL oneUrl = new SIURL("http://localhost/one"); 
		SamInjectionTransaction transaction = executionNode.getTransaction(oneUrl);
		assertNotNull(transaction);

		Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
		Injector injector = transaction.getTransactionInjector();
		assertNotNull(injector);
		assertNotNull(injector.getExistingBinding(interfaceOneKey));
		
		TestInterfaceOne instanceOne = injector.getInstance(interfaceOneKey);
		assertTrue(instanceOne.runTest());
		
	}

	@Test( groups = "transactionsExecution", dependsOnGroups="transactionsCreation")
	public void testInjectionTransactionExecutionForServiceTwo() throws MalformedURLException {
		SIURL url = new SIURL("http://localhost/two"); 
		SamInjectionTransaction transaction = executionNode.getTransaction(url);
		assertNotNull(transaction);

		Key<TestInterfaceTwo0> interfaceTwoKey = Key.get(TestInterfaceTwo0.class);
		Injector injector = transaction.getTransactionInjector();
		assertNotNull(injector);
		assertNotNull(injector.getExistingBinding(interfaceTwoKey));
		
		TestInterfaceTwo0 instanceOne = injector.getInstance(interfaceTwoKey);
		assertTrue(instanceOne.runTest());
		
	}

}
