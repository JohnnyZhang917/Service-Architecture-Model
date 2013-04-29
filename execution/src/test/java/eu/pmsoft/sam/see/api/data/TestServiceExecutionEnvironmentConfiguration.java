package eu.pmsoft.sam.see.api.data;

import eu.pmsoft.sam.see.api.data.architecture.SeeTestArchitecture;
import eu.pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import eu.pmsoft.sam.see.configuration.SEEConfiguration;
import eu.pmsoft.sam.see.configuration.SEEConfigurationBuilder;
import eu.pmsoft.sam.see.configuration.SEEConfigurationGrammar;

public class TestServiceExecutionEnvironmentConfiguration {

//    public static SEEConfiguration createSEEConfiguration(int port) {
//        return createArchitectureConfiguration().bindToAddress(new InetSocketAddress(port));
//    }
//
    public static SEEConfiguration createSEEConfiguration() {
        return null;
    }

    public static SEEConfiguration createArchitectureConfiguration() {
        return SEEConfigurationBuilder.configuration()
                .architecture(new SeeTestArchitecture())
                .implementationPackage(new TestImplementationDeclaration()).build();
    }

}
