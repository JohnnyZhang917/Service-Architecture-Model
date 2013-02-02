package pmsoft.sam.see.api;

import com.google.inject.Key;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pmsoft.exceptions.OperationCheckedException;
import pmsoft.execution.ServiceAction;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.SEEServiceSetupAction;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;
import pmsoft.sam.see.api.data.TestTransactionDefinition;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceTwo0;
import pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;
import pmsoft.sam.see.api.data.architecture.service.*;
import pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import pmsoft.sam.see.api.data.impl.TestServiceZeroModule;
import pmsoft.sam.see.api.data.impl.courier.TestCourierServiceModule;
import pmsoft.sam.see.api.data.impl.shopping.TestShoppingModule;
import pmsoft.sam.see.api.data.impl.store.TestStoreServiceModule;
import pmsoft.sam.see.api.model.ExecutionStrategy;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import pmsoft.sam.see.api.transaction.SamTransactionConfigurationUtil;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class TestSEEexecution {

    @DataProvider(name = "executionStrategies")
    public Object[][] listOfArchitectures() {
        return new Object[][] { { ExecutionStrategy.PROCEDURAL },{ ExecutionStrategy.SIMPLE_LAZY } };
    }

    @Test(dataProvider = "executionStrategies", sequential = true)
    public void testCourierSetup(final ExecutionStrategy strategy) throws ExecutionException, OperationCheckedException {
		int clientPort = 4989;
		int storePort = 4988;
		int courierPort = 4987;
		SEEServer client = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(clientPort));
		client.startUpServer();

        SEEServiceSetupAction storeSetupAction=new SEEServiceSetupAction() {
            @Override
            public void setup()  {
                SIID storeInstanceId = createServiceInstance(TestStoreServiceModule.class);
                setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(StoreService.class).providedByServiceInstance(storeInstanceId), strategy);
            }
        };
		SEEServer store = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(storePort,storeSetupAction));
		store.startUpServer();

		SEEServer courier = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(courierPort));
		courier.startUpServer();

		try {
			courier.executeSetupAction(new SEEServiceSetupAction() {
				@Override
				public void setup() {
					SIID courierInstanceId = createServiceInstance(TestCourierServiceModule.class);
					setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(CourierService.class).providedByServiceInstance(
							courierInstanceId), strategy);
				}
			});
			final SIURL storeURL = extractServiceURL(store, new ServiceKey(StoreService.class));
			final SIURL courierURL = extractServiceURL(courier, new ServiceKey(CourierService.class));
			client.executeSetupAction(new SEEServiceSetupAction() {
				@Override
				public void setup() {
					SIID shoppingInstanceId = createServiceInstance(TestShoppingModule.class);
					setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(ShoppingService.class).urlBinding(StoreService.class, storeURL)
							.urlBinding(CourierService.class, courierURL).providedByServiceInstance(shoppingInstanceId), strategy);
				}
			});

			final SIURL shoppingURL = extractServiceURL(client, new ServiceKey(ShoppingService.class));
            ServiceAction<Integer,ShoppingStoreWithCourierInteraction> interaction = new ServiceAction<Integer,ShoppingStoreWithCourierInteraction>(shoppingURL.getServiceInstanceReference(),
                    Key.get(ShoppingStoreWithCourierInteraction.class)){

                @Override
                public Integer executeInteraction(ShoppingStoreWithCourierInteraction service) {
                    return service.makeShoping();
                }
            };

			Future<Integer> future = client.executeServiceAction(interaction);
            assertNotNull(future);
            int shoppingCount = 0;
            while (!future.isDone()) {
				try {
                    shoppingCount = future.get();
				} catch (InterruptedException e) {
					// ignore
				}
			}
            assertTrue(shoppingCount > 0);
		} finally {
			client.shutdownServer();
			store.shutdownServer();
			courier.shutdownServer();
		}
	}

	@Test
	public void testSEEConfigurationSetup() throws ExecutionException, OperationCheckedException {
		int serverPort = 4996;
		int clientPort = 4995;

		SEEServer clientNode = null;
		SEEServer server = null;
		try {
			server = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(serverPort));
			server.startUpServer();
			clientNode = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(clientPort));
			clientNode.startUpServer();

			server.executeSetupAction(new SEEServiceSetupAction() {
				@Override
				public void setup() {
					SIID zero = createServiceInstance(TestServiceZeroModule.class);
					SIID one = createServiceInstance(TestServiceOneModule.class);
					SIID two = createServiceInstance(TestServiceTwoModule.class);

					SamInjectionConfiguration zeroSingle = TestTransactionDefinition.createServiceZeroConfiguration(zero);
					setupServiceTransaction(zeroSingle, ExecutionStrategy.PROCEDURAL);

					SamInjectionConfiguration oneSingle = TestTransactionDefinition.createServiceOneConfiguration(one);
					setupServiceTransaction(oneSingle, ExecutionStrategy.PROCEDURAL);

					SamInjectionConfiguration twoLocal = TestTransactionDefinition.createServiceTwoConfiguration(two, one, zero);
					setupServiceTransaction(twoLocal, ExecutionStrategy.PROCEDURAL);
				}
			});

			final SIURL serviceZero = extractServiceURL(server, new ServiceKey(TestServiceZero.class));
			final SIURL serviceOne = extractServiceURL(server, new ServiceKey(TestServiceOne.class));

			clientNode.executeSetupAction(new SEEServiceSetupAction() {
				@Override
				public void setup() {
					SIID two = createServiceInstance(TestServiceTwoModule.class);
					SamInjectionConfiguration twoRemote = TestTransactionDefinition.createServiceTwoConfiguration(two, serviceOne, serviceZero);
					setupServiceTransaction(twoRemote, ExecutionStrategy.PROCEDURAL);
				}
			});

			Map<SIURL, ServiceKey> serviceClient = clientNode.getServiceDiscovery().getServiceRunningStatus();
			SIURL serviceTwoLocal = findService(new ServiceKey(TestServiceTwo.class), serviceClient);

            ServiceAction<Boolean,TestInterfaceTwo0> interaction = new ServiceAction<Boolean,TestInterfaceTwo0>(serviceTwoLocal.getServiceInstanceReference(), Key.get(TestInterfaceTwo0.class)) {
				@Override
				public Boolean executeInteraction(TestInterfaceTwo0 service) {
					return service.runTest();
				}
			};
            Future<Boolean> result = clientNode.executeServiceAction(interaction);
            assertNotNull(result);
            Boolean testResultSEEConfigurationSetup = false;
            while (!result.isDone()) {
                try {
                    testResultSEEConfigurationSetup = result.get();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
			assertTrue(testResultSEEConfigurationSetup);

		} finally {
			if (server != null)
				server.shutdownServer();
			if (clientNode != null)
				clientNode.shutdownServer();
		}

	}

	private SIURL extractServiceURL(SEEServer node, ServiceKey serviceKey) {
		SamServiceDiscovery serviceDiscovery = node.getServiceDiscovery();
		Map<SIURL, ServiceKey> services = serviceDiscovery.getServiceRunningStatus();
		final SIURL url = findService(serviceKey, services);
		assertNotNull("can't find service for key " + serviceKey, url);
		return url;
	}

	private SIURL findService(ServiceKey serviceKey, Map<SIURL, ServiceKey> services) {
		for (Entry<SIURL, ServiceKey> entry : services.entrySet()) {
			if (entry.getValue().equals(serviceKey)) {
				return entry.getKey();
			}
		}
		return null;
	}

}
