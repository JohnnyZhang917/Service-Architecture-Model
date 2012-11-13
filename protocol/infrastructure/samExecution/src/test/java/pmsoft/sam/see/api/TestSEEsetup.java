package pmsoft.sam.see.api;

import pmsoft.sam.see.SEEServer;
import pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;

public class TestSEEsetup {

	public void testSEEConfigurationSetup() {
		int serverPort = 4999;
		SEEServer server = new SEEServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(serverPort));
		server.startUpServer();
		server.shutdownServer();
	}
	
}
