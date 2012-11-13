package pmsoft.sam.see.api;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.SEEServiceSetupAction;
import pmsoft.sam.see.ServiceInteracion;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;
import pmsoft.sam.see.api.data.TestTransactionDefinition;
import pmsoft.sam.see.api.data.architecture.contract.TestInterfaceTwo0;
import pmsoft.sam.see.api.data.architecture.service.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.service.TestServiceTwo;
import pmsoft.sam.see.api.data.architecture.service.TestServiceZero;
import pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import pmsoft.sam.see.api.data.impl.TestServiceZeroModule;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

import com.google.inject.Key;

public class TestSEEexecution {


	private static boolean testResultSEEConfigurationSetup = false;
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

			SamServiceDiscovery serviceDiscovery = server.getServiceDiscovery();
			Map<SIURL, ServiceKey> services = serviceDiscovery.getServiceRunningStatus();
			final SIURL serviceZero = findService(new ServiceKey(TestServiceZero.class), services);
			assertNotNull(serviceZero);
			final SIURL serviceOne = findService(new ServiceKey(TestServiceOne.class), services);
			assertNotNull(serviceOne);
			final SIURL serviceTwo = findService(new ServiceKey(TestServiceTwo.class), services);
			assertNotNull(serviceTwo);

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

	private SIURL findService(ServiceKey serviceKey, Map<SIURL, ServiceKey> services) {
		for (Entry<SIURL, ServiceKey> entry : services.entrySet()) {
			if (entry.getValue().equals(serviceKey)) {
				return entry.getKey();
			}
		}
		return null;
	}

}
