package pmsoft.sam.see.api;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import org.testng.annotations.Test;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.SEEServiceSetupAction;
import pmsoft.sam.see.ServiceInteracion;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;
import pmsoft.sam.see.api.data.TestTransactionDefinition;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceTwo0;
import pmsoft.sam.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;
import pmsoft.sam.see.api.data.architecture.service.CourierService;
import pmsoft.sam.see.api.data.architecture.service.ShoppingService;
import pmsoft.sam.see.api.data.architecture.service.StoreService;
import pmsoft.sam.see.api.data.architecture.service.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.service.TestServiceTwo;
import pmsoft.sam.see.api.data.architecture.service.TestServiceZero;
import pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import pmsoft.sam.see.api.data.impl.TestServiceZeroModule;
import pmsoft.sam.see.api.data.impl.courier.TestCourierServiceModule;
import pmsoft.sam.see.api.data.impl.shopping.TestShoppingModule;
import pmsoft.sam.see.api.data.impl.store.TestStoreServiceModule;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;
import pmsoft.sam.see.api.transaction.SamTransactionConfigurationUtil;

import com.google.inject.Key;

public class TestSEEexecution {

	private static int shoppingCount = 0;

	@Test
	public void testCourierSetup() {
		int clientPort = 4989;
		int storePort = 4988;
		int courierPort = 4987;
		SEEServer client = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(clientPort));
		client.startUpServer();
		SEEServer store = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(storePort));
		store.startUpServer();
		SEEServer courier = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(courierPort));
		courier.startUpServer();

		store.executeSetupAction(new SEEServiceSetupAction() {
			@Override
			public void setup() {
				SIID storeInstanceId = createServiceInstance(TestStoreServiceModule.class);
				setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(StoreService.class).providedByServiceInstance(storeInstanceId));
			}
		});
		courier.executeSetupAction(new SEEServiceSetupAction() {
			@Override
			public void setup() {
				SIID courierInstanceId = createServiceInstance(TestCourierServiceModule.class);
				setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(CourierService.class).providedByServiceInstance(courierInstanceId));
			}
		});
		final SIURL storeURL = extractServiceURL(store, new ServiceKey(StoreService.class));
		final SIURL courierURL = extractServiceURL(courier, new ServiceKey(CourierService.class));
		client.executeSetupAction(new SEEServiceSetupAction() {
			@Override
			public void setup() {
				SIID shoppingInstanceId = createServiceInstance(TestShoppingModule.class);
				setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(ShoppingService.class)
						.urlBinding(StoreService.class, storeURL)
						.urlBinding(CourierService.class, courierURL)
						.providedByServiceInstance(shoppingInstanceId));
			}
		});

		final SIURL shoppingURL = extractServiceURL(client, new ServiceKey(ShoppingService.class));
		ServiceInteracion<ShoppingStoreWithCourierInteraction> interaction = new ServiceInteracion<ShoppingStoreWithCourierInteraction>(shoppingURL,
				Key.get(ShoppingStoreWithCourierInteraction.class)) {
			@Override
			public void executeInteraction(ShoppingStoreWithCourierInteraction service) {
				shoppingCount = service.makeShoping();
			}
		};
		client.executeServiceInteraction(interaction);
		assertTrue(shoppingCount > 0);

	}

	private static boolean testResultSEEConfigurationSetup = false;

	@Test
	public void testSEEConfigurationSetup() {
		int serverPort = 4999;
		int clientPort = 4998;

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
					setupServiceTransaction(zeroSingle);

					SamInjectionConfiguration oneSingle = TestTransactionDefinition.createServiceOneConfiguration(one);
					setupServiceTransaction(oneSingle);

					SamInjectionConfiguration twoLocal = TestTransactionDefinition.createServiceTwoConfiguration(two, one, zero);
					setupServiceTransaction(twoLocal);
				}
			});

			final SIURL serviceZero = extractServiceURL(server, new ServiceKey(TestServiceZero.class));
			final SIURL serviceOne = extractServiceURL(server, new ServiceKey(TestServiceOne.class));

			clientNode.executeSetupAction(new SEEServiceSetupAction() {
				@Override
				public void setup() {
					SIID two = createServiceInstance(TestServiceTwoModule.class);
					SamInjectionConfiguration twoRemote = TestTransactionDefinition.createServiceTwoConfiguration(two, serviceOne, serviceZero);
					setupServiceTransaction(twoRemote);
				}
			});

			Map<SIURL, ServiceKey> serviceClient = clientNode.getServiceDiscovery().getServiceRunningStatus();
			SIURL serviceTwoLocal = findService(new ServiceKey(TestServiceTwo.class), serviceClient);

			ServiceInteracion<TestInterfaceTwo0> interaction = new ServiceInteracion<TestInterfaceTwo0>(serviceTwoLocal, Key.get(TestInterfaceTwo0.class)) {
				@Override
				public void executeInteraction(TestInterfaceTwo0 service) {
					testResultSEEConfigurationSetup = service.runTest();
					testResultSEEConfigurationSetup = service.runTest();
				}
			};
			clientNode.executeServiceInteraction(interaction);
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
		assertNotNull("can't find service for key " + serviceKey,url);
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
