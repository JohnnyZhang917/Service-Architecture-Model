package eu.pmsoft.sam.see.api;

import com.google.inject.Key;
import eu.pmsoft.exceptions.OperationCheckedException;
import eu.pmsoft.execution.ServiceAction;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.SEEServer;
import eu.pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;
import eu.pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;
import eu.pmsoft.sam.see.api.data.architecture.service.CourierService;
import eu.pmsoft.sam.see.api.data.architecture.service.ShoppingService;
import eu.pmsoft.sam.see.api.data.architecture.service.StoreService;
import eu.pmsoft.sam.see.api.data.impl.courier.TestCourierServiceModule;
import eu.pmsoft.sam.see.api.data.impl.shopping.TestShoppingModule;
import eu.pmsoft.sam.see.api.data.impl.store.TestStoreServiceModule;
import eu.pmsoft.sam.see.api.infrastructure.SamServiceDiscovery;
import eu.pmsoft.sam.see.api.model.ExecutionStrategy;
import eu.pmsoft.sam.see.api.model.SIID;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.model.STID;
import eu.pmsoft.sam.see.api.transaction.SamTransactionConfigurationUtil;
import eu.pmsoft.sam.see.configuration.SEEServiceSetupAction;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class TestSEEexecution {

    @DataProvider(name = "executionStrategies")
    public Object[][] listOfArchitectures() {
        return new Object[][]{{ExecutionStrategy.PROCEDURAL}, {ExecutionStrategy.SIMPLE_LAZY}};
    }

    public void testCourierSetup(final ExecutionStrategy strategy) throws ExecutionException, OperationCheckedException {
//        int clientPort = 4989;
        int storePort = 4988;
        int courierPort = 4987;
        SEEServer client = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration());
        client.startUpEnvironment();

        SEEServiceSetupAction storeSetupAction = new SEEServiceSetupAction() {
            @Override
            public void setup() {
                SIID storeInstanceId = createServiceInstance(TestStoreServiceModule.class);
                setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(StoreService.class).providedByServiceInstance(storeInstanceId));
            }
        };
//        SEEServer store = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(storePort, storeSetupAction));
        SEEServer store = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration());
        store.startUpEnvironment();

        SEEServer courier = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration());
//        SEEServer courier = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(courierPort));
        courier.startUpEnvironment();

        try {
            courier.executeSetupAction(new SEEServiceSetupAction() {
                @Override
                public void setup() {
                    SIID courierInstanceId = createServiceInstance(TestCourierServiceModule.class);
                    setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(CourierService.class).providedByServiceInstance(
                            courierInstanceId));
                }
            });
            final STID storeURL = extractServiceURL(store, new ServiceKey(StoreService.class));
            final STID courierURL = extractServiceURL(courier, new ServiceKey(CourierService.class));
            client.executeSetupAction(new SEEServiceSetupAction() {
                @Override
                public void setup() {
                    SIID shoppingInstanceId = createServiceInstance(TestShoppingModule.class);
                    setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(ShoppingService.class).urlBinding(StoreService.class, null)
//                    setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(ShoppingService.class).urlBinding(StoreService.class, storeURL)
                            .urlBinding(CourierService.class, null).providedByServiceInstance(shoppingInstanceId));
                }
            });

            final STID shoppingURL = extractServiceURL(client, new ServiceKey(ShoppingService.class));
//            ServiceAction<Integer, ShoppingStoreWithCourierInteraction> interaction = new ServiceAction<Integer, ShoppingStoreWithCourierInteraction>(shoppingURL,
//                    Key.get(ShoppingStoreWithCourierInteraction.class)) {
//
//                @Override
//                public Integer executeInteraction(ShoppingStoreWithCourierInteraction service) {
//                    return service.makeShoping();
//                }
//            };

//            Future<Integer> future = client.executeServiceAction(interaction);
//            assertNotNull(future);
//            int shoppingCount = 0;
//            while (!future.isDone()) {
//                try {
//                    shoppingCount = future.get();
//                } catch (InterruptedException e) {
//                    // ignore
//                }
//            }
//            assertTrue(shoppingCount > 0);
        } finally {
            client.shutdownEnvironment();
            store.shutdownEnvironment();
            courier.shutdownEnvironment();
        }
    }

    @Test
    public void testSEEConfigurationSetup() throws ExecutionException, OperationCheckedException {
        int serverPort = 4996;
        int clientPort = 4995;

        SEEServer clientNode = null;
        SEEServer server = null;
        try {
            server = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration());
            server.startUpEnvironment();
            clientNode = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration());
            clientNode.startUpEnvironment();

            server.executeSetupAction(new SEEServiceSetupAction() {
                @Override
                public void setup() {
//                    SIID zero = createServiceInstance(TestServiceZeroModule.class);
//                    SIID one = createServiceInstance(TestServiceOneModule.class);
//                    SIID two = createServiceInstance(TestServiceTwoModule.class);
//
//                    SamInjectionConfiguration zeroSingle = TestTransactionDefinition.createServiceZeroConfiguration(zero);
//                    setupServiceTransaction(zeroSingle, ExecutionStrategy.PROCEDURAL);
//
//                    SamInjectionConfiguration oneSingle = TestTransactionDefinition.createServiceOneConfiguration(one);
//                    setupServiceTransaction(oneSingle, ExecutionStrategy.PROCEDURAL);
//
//                    SamInjectionConfiguration twoLocal = TestTransactionDefinition.createServiceTwoConfiguration(two, one, zero);
//                    setupServiceTransaction(twoLocal, ExecutionStrategy.PROCEDURAL);
                }
            });

//            final SIURL serviceZero = extractServiceURL(server, new ServiceKey(TestServiceZero.class));
//            final SIURL serviceOne = extractServiceURL(server, new ServiceKey(TestServiceOne.class));

            clientNode.executeSetupAction(new SEEServiceSetupAction() {
                @Override
                public void setup() {
//                    SIID two = createServiceInstance(TestServiceTwoModule.class);
//                    SamInjectionConfiguration twoRemote = TestTransactionDefinition.createServiceTwoConfiguration(two, serviceOne, serviceZero);
//                    setupServiceTransaction(twoRemote, ExecutionStrategy.PROCEDURAL);
                }
            });
//
//            Map<SIURL, ServiceKey> serviceClient = clientNode.getServiceDiscovery().getServiceRunningStatus();
//            SIURL serviceTwoLocal = findService(new ServiceKey(TestServiceTwo.class), serviceClient);
//
//            ServiceAction<Boolean, TestInterfaceTwo0> interaction = new ServiceAction<Boolean, TestInterfaceTwo0>(serviceTwoLocal.getServiceInstanceReference(), Key.get(TestInterfaceTwo0.class)) {
//                @Override
//                public Boolean executeInteraction(TestInterfaceTwo0 service) {
//                    return service.runTest();
//                }
//            };
//            Future<Boolean> result = clientNode.executeServiceAction(interaction);
//            assertNotNull(result);
//            Boolean testResultSEEConfigurationSetup = false;
//            while (!result.isDone()) {
//                try {
//                    testResultSEEConfigurationSetup = result.get();
//                } catch (InterruptedException e) {
//                    // ignore
//                }
//            }
//            assertTrue(testResultSEEConfigurationSetup);

        } finally {
            if (server != null)
                server.shutdownEnvironment();
            if (clientNode != null)
                clientNode.shutdownEnvironment();
        }

    }

    private STID extractServiceURL(SEEServer node, ServiceKey serviceKey) {
//        SamServiceDiscovery serviceDiscovery = node.getServiceDiscovery();
//        Map<STID, ServiceKey> services = serviceDiscovery.getServiceRunningStatus();
//        final STID url = findService(serviceKey, services);
//        assertNotNull("can't find service for key " + serviceKey, url);
//        return url;
        return null;
    }

    private STID findService(ServiceKey serviceKey, Map<STID, ServiceKey> services) {
        for (Entry<STID, ServiceKey> entry : services.entrySet()) {
            if (entry.getValue().equals(serviceKey)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
