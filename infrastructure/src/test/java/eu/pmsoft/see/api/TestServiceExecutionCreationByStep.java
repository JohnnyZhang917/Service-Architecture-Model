package eu.pmsoft.see.api;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.model.*;
import eu.pmsoft.sam.see.*;
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceOne;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceTwo0;
import eu.pmsoft.see.api.data.impl.TestImplementationDeclaration;
import eu.pmsoft.see.api.data.impl.TestServiceOneModule;
import eu.pmsoft.see.api.data.impl.TestServiceTwoModule;
import eu.pmsoft.see.api.data.impl.TestServiceZeroModule;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import scala.collection.immutable.Set;

import javax.inject.Inject;

import static org.testng.Assert.*;

@Guice(modules = {ServiceExecutionEnvironmentModule.class})
public class TestServiceExecutionCreationByStep {

    @Inject
    private SamArchitectureManagement architectureManager;
    @Inject
    private SamServiceRegistry samServiceRegistry;
    @Inject
    private SamExecutionNode executionNode;
//
//    @BeforeMethod
//    public void setUp() throws Exception {
//        executionNode.bindToServer(new SamTransportCommunicationContext() {
//            @Override
//            public SIURL liftServiceTransaction(STID stid) {
//                try {
//                    return SIURL.fromUrlString("http://localhost:8080/anyURl/" + stid.toString());
//                } catch (MalformedURLException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public List<SamTransportChannel> createClientConnectionChannels(UUID transactionID, List<URL> endpointAddressList) {
//                return null;
//            }
//
//        });
//    }

    @DataProvider(name = "architecturesToSetup")
    public Object[][] listOfArchitectures() throws IncorrectArchitectureDefinition {
        return new Object[][]{{SamModelBuilder.loadArchitectureDefinition(new SeeTestArchitecture())}};
    }

    //
    @DataProvider(name = "implementationDeclarations")
    public Object[][] listOfImplementationDeclarations() {
        return new Object[][]{{new TestImplementationDeclaration()}};
    }

    @DataProvider(name = "registeredImplementations")
    public Object[][] listOfProvidedImplementationKeys() {
        return new Object[][]{
                {TestServiceZeroModule.class},
                {TestServiceOneModule.class},
                {TestServiceTwoModule.class}
        };
    }

    @Test(dataProvider = "architecturesToSetup", groups = "architectureSetup")
    public void testLoadOfArchitectureInformation(SamArchitecture architecture) {
        architectureManager.registerArchitecture(architecture);
    }

    @Test(dataProvider = "implementationDeclarations", groups = "architectureDefinition", dependsOnGroups = "architectureSetup")
    public void testRegistrationOfImplementationPackage(SamServiceImplementationPackageContract implementationPackage) {
        Set<SamServiceImplementation> serviceImplementationObjectSet = SamModelBuilder.loadImplementationPackage(implementationPackage);
        samServiceRegistry.registerImplementations(serviceImplementationObjectSet);
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck", dependsOnGroups = "architectureDefinition")
    public void testLoadOfImplementations(Class<? extends Module> serviceKey) {
        SamServiceImplementationKey implementationKey = SamModelBuilder.implementationKey(serviceKey);
        SamServiceImplementation registered = samServiceRegistry.getImplementation(implementationKey);
        assertNotNull(registered);
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck", dependsOnGroups = "architectureDefinition")
    public void testServiceInstanceCreation(Class<? extends Module> implementationModule)  {
        SamServiceImplementationKey key = SamModelBuilder.implementationKey(implementationModule);
        SamServiceInstance serviceInstance = executionNode.createServiceInstance(key);
        assertNotNull(serviceInstance);
        assertNotNull(serviceInstance.implementation());
        assertNotNull(serviceInstance.implementation().contract());
        SamServiceImplementationKey implKey = SamModelBuilder.implementationKey(serviceInstance.implementation().implKey().module());
        assertEquals(implKey, key);
        assertNotNull(serviceInstance.injector());
        assertNotNull(serviceInstance.implementation().contract());
    }

    private SamServiceInstance getUniqueServiceInstance(Class<? extends Module> implementationModule) {
        SamServiceImplementationKey serviceKey = SamModelBuilder.implementationKey(implementationModule);
        Set<SIID> serviceInstances = executionNode.getServiceInstances(serviceKey);

        assertEquals(1, serviceInstances.size(), "1 instance of Service expected");
        SIID id = serviceInstances.iterator().next();
        return executionNode.getInstance(id);
    }


    @Test(groups = "transactionsCreation", dependsOnGroups = "architectureLoadCheck")
    public void testInjectionTransactionCreation() {

        SamServiceInstance zero = getUniqueServiceInstance(TestServiceZeroModule.class);
        SamServiceInstance one = getUniqueServiceInstance(TestServiceOneModule.class);
        SamServiceInstance two = getUniqueServiceInstance(TestServiceTwoModule.class);

        InjectionElement elementZero = InjectionTransactionBuilder.singleInstanceBind(architectureManager, zero);
        assertNotNull(elementZero);
        assertEquals(zero.implementation().contract(), elementZero.contract().id());

        InjectionElement elementOne = InjectionTransactionBuilder.singleInstanceBind(architectureManager, one);
        assertNotNull(elementOne);
        assertEquals(one.implementation().contract(), elementOne.contract().id());

        InjectionElement[] bindings = {elementOne,elementZero};
        InjectionElement elementTwo = InjectionTransactionBuilder.complexInstanceBind(architectureManager, two, bindings);
        assertNotNull(elementTwo);
        assertEquals(two.implementation().contract(), elementTwo.contract().id());


        STID transactionZero = setupAndCheckTransactionRegistration(elementZero);
        STID transactionOne = setupAndCheckTransactionRegistration(elementOne);
        STID transactionTwo = setupAndCheckTransactionRegistration(elementTwo);


        testInjectionTransactionExecutionForServiceOne(transactionOne);
//
        SIURL zeroURL = executionNode.liftServiceTransaction(transactionZero);
        SIURL oneURL = executionNode.liftServiceTransaction(transactionOne);

        SamService serviceOne = architectureManager.getService(one.implementation().contract());
        InjectionElement externalOne = InjectionTransactionBuilder.externalServiceBind(serviceOne, oneURL);

        SamService serviceZero = architectureManager.getService(zero.implementation().contract());
        InjectionElement externalZero = InjectionTransactionBuilder.externalServiceBind(serviceZero, zeroURL);

        InjectionElement[] externalBindings = {externalZero,externalOne};
        InjectionElement elementTwoRemote = InjectionTransactionBuilder.complexInstanceBind(architectureManager, two, externalBindings);
        assertNotNull(elementTwoRemote);
        assertEquals(two.implementation().contract(), elementTwoRemote.contract().id());

        STID siurlRemoteTwo = setupAndCheckTransactionRegistration(elementTwoRemote);

        testInjectionTransactionExecutionForServiceTwo(siurlRemoteTwo);
    }

    private STID setupAndCheckTransactionRegistration(InjectionElement element) {
        STID stid = executionNode.setupInjectionTransaction(element);
        InjectionTransaction transactionRegistered = executionNode.getTransaction(stid);
        assertNotNull(transactionRegistered);
        return stid;
    }

    public void testInjectionTransactionExecutionForServiceOne(STID transactionOne){
        InjectionTransaction transaction = executionNode.getTransaction(transactionOne);

        Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
        Injector injector = transaction.transactionInjector();
        assertNotNull(injector);
        assertNotNull(injector.getExistingBinding(interfaceOneKey));
        // no transaction controller because this is a single local instance
        TestInterfaceOne instanceOne = injector.getInstance(interfaceOneKey);
        assertTrue(instanceOne.runTest());

    }

    public void testInjectionTransactionExecutionForServiceTwo(STID transactionURL) {
        Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
        Key<TestInterfaceTwo0> interfaceTwoKey = Key.get(TestInterfaceTwo0.class);
        InjectionTransaction transaction = executionNode.getTransaction(transactionURL);
        Injector injector = transaction.transactionInjector();

        assertNotNull(injector);
        assertNotNull(injector.getExistingBinding(interfaceTwoKey));
        assertNull(injector.getExistingBinding(interfaceOneKey));

        transaction.rootNode().bindTransaction();
        TestInterfaceTwo0 instanceTwo = injector.getInstance(interfaceTwoKey);
        assertTrue(instanceTwo.runTest());
        transaction.rootNode().unbindTransaction();
    }

}
