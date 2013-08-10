package eu.pmsoft.see.api;

import com.google.inject.Key;
import com.google.inject.Module;
import eu.pmsoft.sam.execution.ServiceAction;
import eu.pmsoft.sam.model.*;
import eu.pmsoft.sam.see.*;
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceOne;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceTwo0;
import eu.pmsoft.see.api.data.impl.TestImplementationDeclaration;
import eu.pmsoft.see.api.data.impl.TestServiceOneModule;
import eu.pmsoft.see.api.data.impl.TestServiceTwoModule;
import eu.pmsoft.see.api.data.impl.TestServiceZeroModule;
import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import scala.collection.immutable.Set;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

@Guice(moduleFactory = TestServiceExecutionCreationByStep.TestEnvironmentApiJavaFactory.class)
public class TestServiceExecutionCreationByStep {

    public static class TestEnvironmentApiJavaFactory implements IModuleFactory {
        @Override
        public Module createModule(ITestContext context, Class<?> testClass) {
            Integer anyPort = 4000;
            SEEConfiguration seeConfiguration = ServiceExecutionEnvironment.configurationBuilder(anyPort)
                    .withArchitecture(new SeeTestArchitecture())
                    .withImplementation(new TestImplementationDeclaration()).build();
            ServiceExecutionEnvironment environment = ServiceExecutionEnvironment.apply(seeConfiguration);
            return environment.clientApiModule();
        }
    }

    @Inject
    private SamArchitectureManagementApi architectureManager;
    @Inject
    private SamServiceRegistryApi samServiceRegistryApi;
    @Inject
    private SamExecutionNodeApi executionNode;
    @Inject
    private SamInjectionTransactionApi transactionApi;

    @DataProvider(name = "registeredImplementations")
    public Object[][] listOfProvidedImplementationKeys() {
        return new Object[][]{
                {TestServiceZeroModule.class},
                {TestServiceOneModule.class},
                {TestServiceTwoModule.class}
        };
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck")
    public void testLoadOfImplementations(Class<? extends Module> serviceKey) {
        SamServiceImplementationKey implementationKey = SamModelBuilder.implementationKey(serviceKey);
        SamServiceImplementation registered = samServiceRegistryApi.getImplementation(implementationKey);
        assertNotNull(registered);
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck")
    public void testServiceInstanceCreation(Class<? extends Module> implementationModule) {
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
        Set<ServiceInstanceID> serviceInstances = executionNode.getServiceInstances(serviceKey);

        assertEquals(1, serviceInstances.size(), "1 instance of Service expected");
        ServiceInstanceID id = serviceInstances.iterator().next();
        return executionNode.getInstance(id);
    }


    @Test(groups = "transactionsCreation", dependsOnGroups = "architectureLoadCheck")
    public void testInjectionTransactionCreation() {

        SamServiceInstance zero = getUniqueServiceInstance(TestServiceZeroModule.class);
        SamServiceInstance one = getUniqueServiceInstance(TestServiceOneModule.class);
        SamServiceInstance two = getUniqueServiceInstance(TestServiceTwoModule.class);

        InjectionConfigurationElement elementZero = InjectionConfigurationBuilder.singleInstanceBind(architectureManager, zero);
        assertNotNull(elementZero);
        assertEquals(zero.implementation().contract(), elementZero.contract().id());

        InjectionConfigurationElement elementOne = InjectionConfigurationBuilder.singleInstanceBind(architectureManager, one);
        assertNotNull(elementOne);
        assertEquals(one.implementation().contract(), elementOne.contract().id());

        InjectionConfigurationElement[] bindings = {elementOne, elementZero};
        InjectionConfigurationElement elementTwo = InjectionConfigurationBuilder.complexInstanceBind(architectureManager, two, bindings);
        assertNotNull(elementTwo);
        assertEquals(two.implementation().contract(), elementTwo.contract().id());


        ServiceConfigurationID transactionZero = setupAndCheckTransactionRegistration(elementZero);
        ServiceConfigurationID transactionOne = setupAndCheckTransactionRegistration(elementOne);
        ServiceConfigurationID transactionTwo = setupAndCheckTransactionRegistration(elementTwo);


        testInjectionTransactionExecutionForServiceOne(transactionOne);
//
        ServiceInstanceURL zeroURL = transactionApi.liftServiceConfiguration(transactionZero);
        ServiceInstanceURL oneURL = transactionApi.liftServiceConfiguration(transactionOne);

        SamService serviceOne = architectureManager.getService(one.implementation().contract());
        InjectionConfigurationElement externalOne = InjectionConfigurationBuilder.externalServiceBind(serviceOne, oneURL);

        SamService serviceZero = architectureManager.getService(zero.implementation().contract());
        InjectionConfigurationElement externalZero = InjectionConfigurationBuilder.externalServiceBind(serviceZero, zeroURL);

        InjectionConfigurationElement[] externalBindings = {externalZero, externalOne};
        InjectionConfigurationElement elementTwoRemote = InjectionConfigurationBuilder.complexInstanceBind(architectureManager, two, externalBindings);
        assertNotNull(elementTwoRemote);
        assertEquals(two.implementation().contract(), elementTwoRemote.contract().id());

        ServiceConfigurationID siurlRemoteTwo = setupAndCheckTransactionRegistration(elementTwoRemote);

        testInjectionTransactionExecutionForServiceTwo(siurlRemoteTwo);
    }

    private ServiceConfigurationID setupAndCheckTransactionRegistration(InjectionConfigurationElement element) {
        ServiceConfigurationID serviceConfigurationID = executionNode.registerInjectionConfiguration(element);
        return serviceConfigurationID;
    }

    public void testInjectionTransactionExecutionForServiceOne(ServiceConfigurationID transactionOne) {
        Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
        Future<Boolean> booleanFuture = transactionApi.executeServiceAction(transactionOne, new ServiceAction<Boolean, TestInterfaceOne>(interfaceOneKey) {
            @Override
            public Boolean executeInteraction(TestInterfaceOne service) {
                return service.runTest();
            }
        });
        Boolean result = false;
        try {
            result = Await.<Boolean>result(booleanFuture, Duration.apply(2, TimeUnit.SECONDS));
        } catch (Exception e) {

        }
        assertTrue(result);
    }

    public void testInjectionTransactionExecutionForServiceTwo(ServiceConfigurationID transactionURL) {
        Key<TestInterfaceTwo0> interfaceTwoKey = Key.get(TestInterfaceTwo0.class);
        Future<Boolean> booleanFuture = transactionApi.executeServiceAction(transactionURL, new ServiceAction<Boolean, TestInterfaceTwo0>(interfaceTwoKey) {
            @Override
            public Boolean executeInteraction(TestInterfaceTwo0 service) {
                return service.runTest();
            }
        });
        Boolean result = false;
        try {
            result = Await.<Boolean>result(booleanFuture, Duration.apply(2, TimeUnit.SECONDS));
        } catch (Exception e) {

        }
        assertTrue(result);
    }

}
