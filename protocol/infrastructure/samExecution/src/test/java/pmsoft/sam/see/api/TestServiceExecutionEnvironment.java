package pmsoft.sam.see.api;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

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
import pmsoft.sam.see.api.data.architecture.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.TestServiceTwo;
import pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import pmsoft.sam.see.api.data.transactions.TestTransactionDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SamInjectionTransaction;
import pmsoft.sam.see.api.model.SamServiceImplementation;
import pmsoft.sam.see.api.model.SamServiceImplementationKey;
import pmsoft.sam.see.api.model.SamServiceInstance;
import pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;

import com.google.inject.Inject;

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
		TestImplementationDeclaration declaration = new TestImplementationDeclaration();
		samServiceRegistry.registerServiceImplementationPackage(declaration);
		SamServiceInstance serviceInstance = executionNode.createServiceInstance(key,null);
		assertNotNull(serviceInstance);
		assertNotNull(serviceInstance.getInjector());
		assertNotNull(serviceInstance.getKey());
	}
	
	@Test( groups = "transactions", dependsOnGroups="architectureLoadCheck")
	public void testInjectionTransactionCreation() {
		SamServiceImplementationKey serviceKeyOne = new SamServiceImplementationKey(TestServiceOneModule.class);
		SamServiceImplementationKey serviceKeyTwo = new SamServiceImplementationKey(TestServiceTwoModule.class);
		Set<SamServiceInstance> setInstanceOne = executionNode.searchInstance(serviceKeyOne, null);
		Set<SamServiceInstance> setInstanceTwo = executionNode.searchInstance(serviceKeyTwo, null);
		assertEquals("1 instance of Service One expected",1, setInstanceOne.size());
		assertEquals("1 instance of Service Two expected",1, setInstanceTwo.size());
		SamServiceInstance instanceOne = setInstanceOne.iterator().next();
		SamServiceInstance instanceTwo = setInstanceTwo.iterator().next();
		SIID siidOne = instanceOne.getKey();
		SIID siidTwo = instanceTwo.getKey();
		

		ServiceKey serviceOneTypeKey = new ServiceKey(TestServiceOne.class);
		ServiceKey serviceTwoTypeKey = new ServiceKey(TestServiceTwo.class);
		
		SamInjectionTransaction transactionOne = TestTransactionDefinition.createServiceOneTransaction(siidOne);
		assertNotNull(transactionOne);
		assertEquals(siidOne, transactionOne.getExposedServiceInstance());
		assertEquals(serviceOneTypeKey, transactionOne.getProvidedService());
		
		SamInjectionTransaction transactionTwo = TestTransactionDefinition.createServiceTwoTransaction(siidTwo, siidOne);
		assertNotNull(transactionTwo);
		assertEquals(siidTwo, transactionTwo.getExposedServiceInstance());
		assertEquals(siidOne, transactionTwo.getInternalInjectionConfiguration().get(serviceOneTypeKey));
		assertEquals(serviceTwoTypeKey, transactionTwo.getProvidedService());
	}
}
