package pmsoft.sam.see.api;

import org.testng.annotations.Test;

import pmsoft.exceptions.OperationCheckedException;
import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;

public class TestSEEsetup {
	
	@Test
	public void testSEEConfigurationSetup() throws OperationCheckedException {
		int serverPort = 4999;
		SEEServer server = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(serverPort));
		server.startUpServer();
		server.shutdownServer();
	}
	
}
