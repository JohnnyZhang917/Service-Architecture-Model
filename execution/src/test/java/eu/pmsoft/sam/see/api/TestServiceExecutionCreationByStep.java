package eu.pmsoft.sam.see.api;

import com.google.inject.*;
import eu.pmsoft.exceptions.OperationCheckedException;
import eu.pmsoft.exceptions.OperationReportingModule;
import eu.pmsoft.injectionUtils.logger.LoggerInjectorModule;
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition;
import eu.pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import eu.pmsoft.sam.architecture.model.SamArchitecture;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.protocol.TransactionController;
import eu.pmsoft.sam.see.api.data.TestTransactionDefinition;
import eu.pmsoft.sam.see.api.data.architecture.SeeTestArchitecture;
import eu.pmsoft.sam.see.api.data.architecture.contract.TestInterfaceOne;
import eu.pmsoft.sam.see.api.data.architecture.contract.TestInterfaceTwo0;
import eu.pmsoft.sam.see.api.data.architecture.service.TestServiceOne;
import eu.pmsoft.sam.see.api.data.architecture.service.TestServiceTwo;
import eu.pmsoft.sam.see.api.data.architecture.service.TestServiceZero;
import eu.pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import eu.pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import eu.pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import eu.pmsoft.sam.see.api.data.impl.TestServiceZeroModule;
import eu.pmsoft.sam.see.api.model.*;
import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import eu.pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.util.Set;

import static org.testng.Assert.*;


@Guice(modules = {OperationReportingModule.class, LocalSeeExecutionModule.class, LocalSeeInfrastructureModule.class, TestSamEnvironment.class, LoggerInjectorModule.class})
public class TestServiceExecutionCreationByStep {

    @Inject
    private SamArchitectureManagement architectureManager;
    @Inject
    private SamServiceRegistry samServiceRegistry;
    @Inject
    private SamExecutionNode executionNode;

    @DataProvider(name = "architecturesToSetup")
    public Object[][] listOfArchitectures() throws IncorrectArchitectureDefinition {
        return new Object[][]{{ArchitectureModelLoader.loadArchitectureModel(new SeeTestArchitecture())}};
    }

    @DataProvider(name = "implementationDeclarations")
    public Object[][] listOfImplementationDeclarations() {
        return new Object[][]{{new TestImplementationDeclaration()}};
    }

    @DataProvider(name = "registeredImplementations")
    public Object[][] listOfProvidedImplementationKeys() {
        return new Object[][]{{new SamServiceImplementationKey(TestServiceZeroModule.class)},
                {new SamServiceImplementationKey(TestServiceOneModule.class)},
                {new SamServiceImplementationKey(TestServiceTwoModule.class)}};
    }

    @BeforeMethod
    public void setUp() throws Exception {
        siurlRemoteTwo = SIURL.fromUrlString("http://remote/two");
        siurlRemoteOne = SIURL.fromUrlString("http://remote/one");
        siurlRemoteZero = SIURL.fromUrlString("http://remote/zero");
        siurlLocalTwo = SIURL.fromUrlString("http://localhost/two");
        siurlLocalOne = SIURL.fromUrlString("http://localhost/one");
        siurlLocalZero = SIURL.fromUrlString("http://localhost/zero");
    }

    @Test(dataProvider = "architecturesToSetup", groups = "architectureSetup")
    public void testLoadOfArchitectureInformation(SamArchitecture architecture) {
        architectureManager.registerArchitecture(architecture);
    }


    @Test(dataProvider = "implementationDeclarations", groups = "architectureDefinition", dependsOnGroups = "architectureSetup")
    public void testRegistrationOfImplementationPackage(SamServiceImplementationPackageContract declaration) {
        samServiceRegistry.registerServiceImplementationPackage(declaration);
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck", dependsOnGroups = "architectureDefinition")
    public void testLoadOfImplementations(SamServiceImplementationKey serviceKey) {
        SamServiceImplementation registered = samServiceRegistry.getImplementation(serviceKey);
        assertNotNull(registered);
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck", dependsOnGroups = "architectureDefinition")
    public void testServiceInstanceCreation(SamServiceImplementationKey key) throws OperationCheckedException {

        SamServiceInstance serviceInstance = executionNode.createServiceInstance(key, null);
        assertNotNull(serviceInstance);
        assertNotNull(serviceInstance.getInjector());
        assertNotNull(serviceInstance.getKey());
    }

    private SIID getUniqueServiceInstance(Class<? extends Module> implementationModule) {
        SamServiceImplementationKey serviceKey = new SamServiceImplementationKey(implementationModule);

        Set<SamServiceInstance> setInstance = executionNode.searchInstance(serviceKey, null);

        assertEquals(1, setInstance.size(), "1 instance of Service expected");
        SamServiceInstance instance = setInstance.iterator().next();
        return instance.getKey();
    }

    SIURL siurlLocalZero;
    SIURL siurlLocalOne;
    SIURL siurlLocalTwo;

    SIURL siurlRemoteZero;
    SIURL siurlRemoteOne;
    SIURL siurlRemoteTwo;


    @Test(groups = "transactionsCreation", dependsOnGroups = "architectureLoadCheck")
    public void testInjectionTransactionCreation() throws MalformedURLException {
        SIID siidZero = getUniqueServiceInstance(TestServiceZeroModule.class);
        SIID siidOne = getUniqueServiceInstance(TestServiceOneModule.class);
        SIID siidTwo = getUniqueServiceInstance(TestServiceTwoModule.class);

        ServiceKey serviceZeroTypeKey = new ServiceKey(TestServiceZero.class);
        ServiceKey serviceOneTypeKey = new ServiceKey(TestServiceOne.class);
        ServiceKey serviceTwoTypeKey = new ServiceKey(TestServiceTwo.class);

        SamInjectionConfiguration transactionZero = TestTransactionDefinition.createServiceZeroConfiguration(siidZero);
        assertNotNull(transactionZero);
        assertEquals(siidZero, transactionZero.getExposedServiceInstance());
        assertEquals(serviceZeroTypeKey, transactionZero.getProvidedService());

        SamInjectionConfiguration transactionOne = TestTransactionDefinition.createServiceOneConfiguration(siidOne);
        assertNotNull(transactionOne);
        assertEquals(siidOne, transactionOne.getExposedServiceInstance());
        assertEquals(serviceOneTypeKey, transactionOne.getProvidedService());

        SamInjectionConfiguration transactionTwo = TestTransactionDefinition.createServiceTwoConfiguration(siidTwo, siidOne, siidZero);
        assertNotNull(transactionTwo);
        assertEquals(siidTwo, transactionTwo.getExposedServiceInstance());
        assertEquals(serviceTwoTypeKey, transactionTwo.getProvidedService());

        setupAndCheckTransactionRegistration(siurlLocalZero, transactionZero);
        setupAndCheckTransactionRegistration(siurlLocalOne, transactionOne);
        setupAndCheckTransactionRegistration(siurlLocalTwo, transactionTwo);

        SamInjectionConfiguration transactionTwoRemote = TestTransactionDefinition.createServiceTwoConfiguration(siidTwo, siurlLocalOne, siurlLocalZero);
        assertNotNull(transactionTwoRemote);
        assertEquals(siidTwo, transactionTwoRemote.getExposedServiceInstance());
        assertEquals(serviceTwoTypeKey, transactionTwoRemote.getProvidedService());

        setupAndCheckTransactionRegistration(siurlRemoteTwo, transactionTwoRemote);
    }

    private void setupAndCheckTransactionRegistration(SIURL url, SamInjectionConfiguration transaction) {
        url = executionNode.setupInjectionTransaction(transaction, url, ExecutionStrategy.PROCEDURAL);
        SamInstanceTransaction transactionRegistered = executionNode.getTransaction(url);
        assertNotNull(transactionRegistered);
    }

    @Test(groups = "transactionsExecution", dependsOnGroups = "transactionsCreation")
    public void testInjectionTransactionExecutionForServiceOne() throws MalformedURLException {

        CanonicalProtocolThreadExecutionContext executionContext = executionNode.createTransactionExecutionContext(siurlLocalOne);
        Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
        Injector injector = executionContext.getInjector();
        assertNotNull(injector);
        assertNotNull(injector.getExistingBinding(interfaceOneKey));
        // no transaction controller because this is a single local instance
        TestInterfaceOne instanceOne = injector.getInstance(interfaceOneKey);
        assertTrue(instanceOne.runTest());

    }

    @DataProvider(name = "transactionTypeTwo")
    public Object[][] transactionTypeTwo() {
        return new Object[][]{{siurlLocalTwo}};
    }

    @Test(groups = "transactionsExecution", dependsOnGroups = "transactionsCreation", dataProvider = "transactionTypeTwo")
    public void testInjectionTransactionExecutionForServiceTwo(SIURL transactionURL) throws MalformedURLException {
        Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
        Key<TestInterfaceTwo0> interfaceTwoKey = Key.get(TestInterfaceTwo0.class);
        CanonicalProtocolThreadExecutionContext executionContext = executionNode.createTransactionExecutionContext(transactionURL);
        Injector injector = executionContext.getInjector();

        assertNotNull(injector);
        assertNotNull(injector.getExistingBinding(interfaceTwoKey));
        assertNull(injector.getExistingBinding(interfaceOneKey));

        TransactionController transactionController = executionContext.getTransactionController();

        transactionController.enterTransactionContext();
        TestInterfaceTwo0 instanceTwo = injector.getInstance(interfaceTwoKey);
        assertTrue(instanceTwo.runTest());
        transactionController.exitTransactionContext();
    }

}
