package pmsoft.sam.see.api;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pmsoft.sam.architecture.loader.IncorrectArchitectureDefinition;
import pmsoft.sam.see.SEEConfiguration;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;

public class TestSEEsetup {

	@DataProvider(name = "seeConfigurations")
	public Object[][] listOfArchitectures() throws IncorrectArchitectureDefinition {
		return new Object[][] { { TestServiceExecutionEnvironmentConfiguration.createTestConfiguration()} };
	}

	@Test( dataProvider = "seeConfigurations")
	public void testSEEConfigurationSetup(SEEConfiguration configuration) {
		SEEServer server = new SEEServer(configuration);
		server.startUpServer();
		server.shutdownServer();
	}
	
}
