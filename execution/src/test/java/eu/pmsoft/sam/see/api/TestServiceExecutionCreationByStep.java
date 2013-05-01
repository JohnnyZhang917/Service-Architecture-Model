package eu.pmsoft.sam.see.api;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import eu.pmsoft.exceptions.OperationCheckedException;
import eu.pmsoft.exceptions.OperationReportingModule;
import eu.pmsoft.injectionUtils.logger.LoggerInjectorModule;
import eu.pmsoft.sam.architecture.model.SamArchitecture;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.protocol.TransactionController;
import eu.pmsoft.sam.see.api.data.TestTransactionDefinition;
import eu.pmsoft.sam.see.api.data.architecture.contract.TestInterfaceOne;
import eu.pmsoft.sam.see.api.data.architecture.contract.TestInterfaceTwo0;
import eu.pmsoft.sam.see.api.data.architecture.service.TestServiceOne;
import eu.pmsoft.sam.see.api.data.architecture.service.TestServiceTwo;
import eu.pmsoft.sam.see.api.data.architecture.service.TestServiceZero;
import eu.pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import eu.pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import eu.pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import eu.pmsoft.sam.see.api.data.impl.TestServiceZeroModule;
import eu.pmsoft.sam.see.api.infrastructure.SamArchitectureManagement;
import eu.pmsoft.sam.see.api.infrastructure.SamServiceRegistryDeprecated;
import eu.pmsoft.sam.see.api.model.*;
import eu.pmsoft.sam.see.api.setup.SamExecutionNodeInternalApi;
import eu.pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import eu.pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import eu.pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;
import eu.pmsoft.sam.see.transport.SamTransportChannel;
import eu.pmsoft.sam.see.transport.SamTransportCommunicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.testng.Assert.*;


@Guice(modules = {OperationReportingModule.class, LocalSeeExecutionModule.class, LocalSeeInfrastructureModule.class, TestSamEnvironment.class, LoggerInjectorModule.class})
public class TestServiceExecutionCreationByStep {

    @Inject
    private SamArchitectureManagement architectureManager;
    @Inject
    private SamServiceRegistryDeprecated samServiceRegistryDeprecated;
    @Inject
    private SamExecutionNodeInternalApi executionNode;

    @BeforeMethod
    public void setUp() throws Exception {
        executionNode.bindToServer(new SamTransportCommunicationContext() {
            @Override
            public SIURL liftServiceTransaction(STID stid) {
                try {
                    return SIURL.fromUrlString("http://localhost:8080/anyURl/" + stid.toString());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<SamTransportChannel> createClientConnectionChannels(UUID transactionID, List<URL> endpointAddressList) {
                return null;
            }

        });
    }

//    @DataProvider(name = "architecturesToSetup")
//    public Object[][] listOfArchitectures() throws IncorrectArchitectureDefinition {
//        return new Object[][]{{ArchitectureModelLoader.loadArchitectureModel(new SeeTestArchitecture())}};
//    }

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

    @Test(dataProvider = "architecturesToSetup", groups = "architectureSetup")
    public void testLoadOfArchitectureInformation(SamArchitecture architecture) {
        architectureManager.registerArchitecture(architecture);
    }


    @Test(dataProvider = "implementationDeclarations", groups = "architectureDefinition", dependsOnGroups = "architectureSetup")
    public void testRegistrationOfImplementationPackage(SamServiceImplementationPackageContract declaration) {
        samServiceRegistryDeprecated.registerServiceImplementationPackage(declaration);
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck", dependsOnGroups = "architectureDefinition")
    public void testLoadOfImplementations(SamServiceImplementationKey serviceKey) {
        SamServiceImplementationDeprecated registered = samServiceRegistryDeprecated.getImplementation(serviceKey);
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

        STID siurlLocalZero = setupAndCheckTransactionRegistration(transactionZero);
        STID siurlLocalOne = setupAndCheckTransactionRegistration(transactionOne);
        STID siurlLocalTwo = setupAndCheckTransactionRegistration(transactionTwo);

        testInjectionTransactionExecutionForServiceOne(siurlLocalOne);

        SIURL zeroURL = executionNode.exposeInjectionConfiguration(siurlLocalZero);
        SIURL oneURL = executionNode.exposeInjectionConfiguration(siurlLocalOne);


        SamInjectionConfiguration transactionTwoRemote = TestTransactionDefinition.createServiceTwoConfiguration(siidTwo, oneURL, zeroURL);
        assertNotNull(transactionTwoRemote);
        assertEquals(siidTwo, transactionTwoRemote.getExposedServiceInstance());
        assertEquals(serviceTwoTypeKey, transactionTwoRemote.getProvidedService());

        STID siurlRemoteTwo = setupAndCheckTransactionRegistration(transactionTwoRemote);

        testInjectionTransactionExecutionForServiceTwo(siurlLocalTwo);
    }

    private STID setupAndCheckTransactionRegistration(SamInjectionConfiguration transaction) {
        STID stid = executionNode.setupInjectionTransaction(transaction);
        SamServiceInstanceTransaction transactionRegistered = executionNode.getTransaction(stid);
        assertNotNull(transactionRegistered);
        return stid;
    }

    public void testInjectionTransactionExecutionForServiceOne(STID localUrl) throws MalformedURLException {

        CanonicalProtocolThreadExecutionContext executionContext = executionNode.createTransactionExecutionContext(localUrl);
        Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
        Injector injector = executionContext.getInjector();
        assertNotNull(injector);
        assertNotNull(injector.getExistingBinding(interfaceOneKey));
        // no transaction controller because this is a single local instance
        TestInterfaceOne instanceOne = injector.getInstance(interfaceOneKey);
        assertTrue(instanceOne.runTest());

    }

    public void testInjectionTransactionExecutionForServiceTwo(STID transactionURL) throws MalformedURLException {
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
