package eu.pmsoft.sam.see.api;

import eu.pmsoft.exceptions.OperationCheckedException;
import eu.pmsoft.sam.see.SEEServer;
import eu.pmsoft.sam.see.api.data.TestServiceExecutionEnvironmentConfiguration;
import org.testng.annotations.Test;

public class TestSEEsetup {

    @Test
    public void testSEEConfigurationSetup() throws OperationCheckedException {
        int serverPort = 4999;
        SEEServer server = SEEServer.createServer(TestServiceExecutionEnvironmentConfiguration.createSEEConfiguration(serverPort));
        server.startUpServer();
        server.shutdownServer();
    }

}
