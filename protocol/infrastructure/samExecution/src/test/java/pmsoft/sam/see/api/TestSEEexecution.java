package pmsoft.sam.see.api;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.Map;
import java.util.Map.Entry;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pmsoft.sam.architecture.loader.IncorrectArchitectureDefinition;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.SEEConfiguration;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;
import pmsoft.sam.see.api.data.architecture.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.TestServiceTwo;
import pmsoft.sam.see.api.model.SIURL;

public class TestSEEexecution {

	@DataProvider(name = "seeConfigurations")
	public Object[][] listOfArchitectures() throws IncorrectArchitectureDefinition {
		return new Object[][] { { TestServiceExecutionEnvironmentConfiguration.createTestConfiguration()} };
	}

	@Test( dataProvider = "seeConfigurations")
	public void testSEEConfigurationSetup(SEEConfiguration configuration) {
		SEEServer server = new SEEServer(configuration);
		server.startUpServer();
		server.shutdownServer();
		
		SamServiceDiscovery serviceDiscovery = server.getServiceDiscovery();
		Map<SIURL, ServiceKey> services = serviceDiscovery.getServiceRunningStatus();
		SIURL serviceOne = findService(new ServiceKey(TestServiceOne.class),services);
		assertNotNull(serviceOne);
		SIURL serviceTwo = findService(new ServiceKey(TestServiceTwo.class),services);
		assertNotNull(serviceTwo);
	}

	private SIURL findService(ServiceKey serviceKey, Map<SIURL, ServiceKey> services) {
		for (Entry<SIURL, ServiceKey> entry : services.entrySet()) {
			if(entry.getValue().equals(serviceKey)) {
				return entry.getKey();
			}
		}
		return null;
	}

}
