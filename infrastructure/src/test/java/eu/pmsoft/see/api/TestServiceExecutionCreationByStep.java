/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.see.api;

import com.google.inject.Key;
import com.google.inject.Module;
import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import eu.pmsoft.sam.execution.ServiceAction;
import eu.pmsoft.sam.model.*;
import eu.pmsoft.sam.see.*;
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceOne;
import eu.pmsoft.see.api.data.architecture.contract.TestInterfaceTwo0;
import eu.pmsoft.see.api.data.architecture.service.TestServiceOne;
import eu.pmsoft.see.api.data.architecture.service.TestServiceTwo;
import eu.pmsoft.see.api.data.architecture.service.TestServiceZero;
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
                {TestServiceZeroModule.class, TestServiceZero.class},
                {TestServiceOneModule.class, TestServiceOne.class},
                {TestServiceTwoModule.class, TestServiceTwo.class}
        };
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck")
    public void testLoadOfImplementations(Class<? extends Module> implementationClass, Class<? extends AbstractSamServiceDefinition> contractClass) {
        SamServiceImplementationKey implementationKey = SamModelBuilder.implementationKey(implementationClass, new SamServiceKey(contractClass));
        SamServiceImplementation registered = samServiceRegistryApi.getImplementation(implementationKey);
        assertNotNull(registered);
    }

    @Test(dataProvider = "registeredImplementations", groups = "architectureLoadCheck")
    public void testServiceInstanceCreation(Class<? extends Module> implementationClass, Class<? extends AbstractSamServiceDefinition> contractClass) {
        SamServiceImplementationKey implementationKey = SamModelBuilder.implementationKey(implementationClass, new SamServiceKey(contractClass));
        SamServiceInstance serviceInstance = executionNode.createServiceInstance(implementationKey);
        assertNotNull(serviceInstance);
        assertNotNull(serviceInstance.implementation());
        assertNotNull(serviceInstance.implementation().implKey());
        assertNotNull(serviceInstance.implementation().implKey().contract());
        SamServiceImplementationKey implKey = SamModelBuilder.implementationKey(serviceInstance.implementation().implKey().module(), serviceInstance.implementation().implKey().contract());
        assertEquals(implKey, implementationKey);
        assertNotNull(serviceInstance.injector());
        assertNotNull(serviceInstance.implementation().implKey());
        assertNotNull(serviceInstance.implementation().implKey().contract());
    }

    private SamServiceInstance getUniqueServiceInstance(Class<? extends Module> implementationClass, Class<? extends AbstractSamServiceDefinition> contractClass) {
        SamServiceImplementationKey implementationKey = SamModelBuilder.implementationKey(implementationClass, new SamServiceKey(contractClass));
        Set<ServiceInstanceID> serviceInstances = executionNode.getServiceInstances(implementationKey);

        assertEquals(1, serviceInstances.size(), "1 instance of Service expected");
        ServiceInstanceID id = serviceInstances.iterator().next();
        return executionNode.getInstance(id);
    }


    @Test(groups = "transactionsExecution", dependsOnGroups = "architectureLoadCheck")
    public void testTransactionExecution() {

        // Get service Instances
        SamServiceInstance zero = getUniqueServiceInstance(TestServiceZeroModule.class, TestServiceZero.class);
        SamServiceInstance one = getUniqueServiceInstance(TestServiceOneModule.class, TestServiceOne.class);
        SamServiceInstance two = getUniqueServiceInstance(TestServiceTwoModule.class, TestServiceTwo.class);

        // Create configuration
        // Service0 alone
        InjectionConfigurationElement elementZero = InjectionConfigurationBuilder.singleInstanceBind(architectureManager, zero);
        assertNotNull(elementZero);
        assertEquals(zero.implementation().implKey().contract(), elementZero.contractKey());

        // Service1 alone
        InjectionConfigurationElement elementOne = InjectionConfigurationBuilder.singleInstanceBind(architectureManager, one);
        assertNotNull(elementOne);
        assertEquals(one.implementation().implKey().contract(), elementOne.contractKey());

        // Service1 bind to Service0 and Service1
        InjectionConfigurationElement[] bindings = {elementOne, elementZero};
        InjectionConfigurationElement elementTwo = InjectionConfigurationBuilder.complexInstanceBind(architectureManager, two, bindings);
        assertNotNull(elementTwo);
        assertEquals(two.implementation().implKey().contract(), elementTwo.contractKey());

        // Create configurations
        ServiceConfigurationID transactionZero = registerConfiguration(elementZero);
        ServiceConfigurationID transactionOne = registerConfiguration(elementOne);
        ServiceConfigurationID transactionTwo = registerConfiguration(elementTwo);

        testInjectionTransactionExecutionForServiceOne(transactionOne);

    }

    @Test(groups = "transactionsExecutionComplex", dependsOnGroups = "transactionsExecution")
    public void testComplexTransactionExecution() {

        // Get service Instances
        SamServiceInstance zero = getUniqueServiceInstance(TestServiceZeroModule.class, TestServiceZero.class);
        SamServiceInstance one = getUniqueServiceInstance(TestServiceOneModule.class, TestServiceOne.class);
        SamServiceInstance two = getUniqueServiceInstance(TestServiceTwoModule.class, TestServiceTwo.class);

        // Create configuration
        // Service0 alone
        InjectionConfigurationElement elementZero = InjectionConfigurationBuilder.singleInstanceBind(architectureManager, zero);
        assertNotNull(elementZero);
        assertEquals(zero.implementation().implKey().contract(), elementZero.contractKey());

        // Service1 alone
        InjectionConfigurationElement elementOne = InjectionConfigurationBuilder.singleInstanceBind(architectureManager, one);
        assertNotNull(elementOne);
        assertEquals(one.implementation().implKey().contract(), elementOne.contractKey());

        // Service1 bind to Service0 and Service1
        InjectionConfigurationElement[] bindings = {elementOne, elementZero};
        InjectionConfigurationElement elementTwo = InjectionConfigurationBuilder.complexInstanceBind(architectureManager, two, bindings);
        assertNotNull(elementTwo);
        assertEquals(two.implementation().implKey().contract(), elementTwo.contractKey());

        // Create configurations
        ServiceConfigurationID transactionZero = registerConfiguration(elementZero);
        ServiceConfigurationID transactionOne = registerConfiguration(elementOne);
        ServiceConfigurationID transactionTwo = registerConfiguration(elementTwo);

        ServiceInstanceURL zeroURL = executionNode.liftServiceConfiguration(transactionZero).url();
        ServiceInstanceURL oneURL = executionNode.liftServiceConfiguration(transactionOne).url();

        SamService serviceOne = architectureManager.getService(one.implementation().implKey().contract());
        InjectionConfigurationElement externalOne = InjectionConfigurationBuilder.externalServiceBind(serviceOne.id(), oneURL);

        SamService serviceZero = architectureManager.getService(zero.implementation().implKey().contract());
        InjectionConfigurationElement externalZero = InjectionConfigurationBuilder.externalServiceBind(serviceZero.id(), zeroURL);

        InjectionConfigurationElement[] externalBindings = {externalZero, externalOne};
        InjectionConfigurationElement elementTwoRemote = InjectionConfigurationBuilder.complexInstanceBind(architectureManager, two, externalBindings);
        assertNotNull(elementTwoRemote);
        assertEquals(two.implementation().implKey().contract(), elementTwoRemote.contractKey());

        ServiceConfigurationID siurlRemoteTwo = registerConfiguration(elementTwoRemote);

        testInjectionTransactionExecutionForServiceTwo(siurlRemoteTwo);
    }

    private ServiceConfigurationID registerConfiguration(InjectionConfigurationElement element) {
        ServiceConfigurationID serviceConfigurationID = executionNode.registerInjectionConfiguration(element);
        return serviceConfigurationID;
    }

    public void testInjectionTransactionExecutionForServiceOne(ServiceConfigurationID transactionOne) {
        Key<TestInterfaceOne> interfaceOneKey = Key.get(TestInterfaceOne.class);
        LiftedServiceConfiguration liftedServiceConfiguration = executionNode.liftServiceConfiguration(transactionOne);
        Future<Boolean> booleanFuture = transactionApi.executeServiceAction(liftedServiceConfiguration, new ServiceAction<Boolean, TestInterfaceOne>(interfaceOneKey) {
            @Override
            public Boolean executeInteraction(TestInterfaceOne service) {
                return service.runTest();
            }
        });
        Boolean result = false;
        try {
            result = Await.<Boolean>result(booleanFuture, Duration.apply(2, TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertTrue(result);
    }

    public void testInjectionTransactionExecutionForServiceTwo(ServiceConfigurationID configurationID) {
        Key<TestInterfaceTwo0> interfaceTwoKey = Key.get(TestInterfaceTwo0.class);
        LiftedServiceConfiguration liftedServiceConfiguration = executionNode.liftServiceConfiguration(configurationID);
        Future<Boolean> booleanFuture = transactionApi.executeServiceAction(liftedServiceConfiguration, new ServiceAction<Boolean, TestInterfaceTwo0>(interfaceTwoKey) {
            @Override
            public Boolean executeInteraction(TestInterfaceTwo0 service) {
                return service.runTest();
            }
        });
        Boolean result = false;
        try {
            result = Await.<Boolean>result(booleanFuture, Duration.apply(2, TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertTrue(result);
    }

}
