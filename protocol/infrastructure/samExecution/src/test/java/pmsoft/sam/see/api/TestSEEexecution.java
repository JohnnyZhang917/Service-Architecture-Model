package pmsoft.sam.see.api;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.inject.Key;

import pmsoft.sam.architecture.loader.IncorrectArchitectureDefinition;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.SEEConfiguration;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.ServiceInteracion;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;
import pmsoft.sam.see.api.data.architecture.TestInterfaceTwo0;
import pmsoft.sam.see.api.data.architecture.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.TestServiceTwo;
import pmsoft.sam.see.api.model.SIURL;

public class TestSEEexecution {

	private static final int serverPort = 4999;
	private static final int clientPort = 4998;

	@DataProvider(name = "seeConfigurations")
	public Object[][] listOfArchitectures() throws IncorrectArchitectureDefinition {
		return new Object[][] { { TestServiceExecutionEnvironmentConfiguration.createTestServerConfiguration(serverPort) } };
	}
	
	boolean testResult = false;

	@Test(dataProvider = "seeConfigurations")
	public void testSEEConfigurationSetup(SEEConfiguration serverConfiguration) {
		SEEServer clientNode = null;
		SEEServer server = null;
		try {
			server = new SEEServer(serverConfiguration);
			server.startUpServer();

			SamServiceDiscovery serviceDiscovery = server.getServiceDiscovery();
			Map<SIURL, ServiceKey> services = serviceDiscovery.getServiceRunningStatus();
			SIURL serviceOne = findService(new ServiceKey(TestServiceOne.class), services);
			assertNotNull(serviceOne);
			SIURL serviceTwo = findService(new ServiceKey(TestServiceTwo.class), services);
			assertNotNull(serviceTwo);

			
			SEEConfiguration clientConfiguration = TestServiceExecutionEnvironmentConfiguration.createTestClientConfiguration(clientPort, serviceOne);
			clientNode = new SEEServer(clientConfiguration);
			clientNode.startUpServer();

			Map<SIURL, ServiceKey> serviceClient = clientNode.getServiceDiscovery().getServiceRunningStatus();
			SIURL serviceTwoLocal = findService(new ServiceKey(TestServiceTwo.class), serviceClient);
			
			ServiceInteracion<TestInterfaceTwo0> interaction = new ServiceInteracion<TestInterfaceTwo0>(serviceTwoLocal,Key.get(TestInterfaceTwo0.class)) {
				@Override
				public void executeInteraction(TestInterfaceTwo0 service) {
					testResult = service.runTest();
				}
			};
			clientNode.executeServiceInteraction(interaction);
			assertTrue(testResult);

		} finally {
			if(server != null) server.shutdownServer();
			if(clientNode != null) clientNode.shutdownServer();
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
