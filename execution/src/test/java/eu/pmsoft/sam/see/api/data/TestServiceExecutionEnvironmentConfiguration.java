package eu.pmsoft.sam.see.api.data;

import com.google.inject.AbstractModule;
import com.google.inject.internal.UniqueAnnotations;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.SEEServiceSetupAction;
import eu.pmsoft.sam.see.api.data.architecture.SeeTestArchitecture;
import eu.pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import eu.pmsoft.sam.see.api.model.SIURL;
import eu.pmsoft.sam.see.api.plugin.SamServiceDiscoveryListener;
import eu.pmsoft.sam.see.configuration.SEEConfiguration;
import eu.pmsoft.sam.see.configuration.SEEConfigurationBuilder;
import eu.pmsoft.sam.see.configuration.SEEConfigurationGrammar;

import java.net.InetSocketAddress;

public class TestServiceExecutionEnvironmentConfiguration {

    public static SEEConfiguration createSEEConfiguration(int port) {
        return createArchitectureConfiguration().bindToAddress(new InetSocketAddress(port));
    }

    private static SEEConfigurationGrammar createArchitectureConfiguration() {
        return SEEConfigurationBuilder.configuration().withPlugin(new AbstractModule() {
            @Override
            protected void configure() {
                bind(SamServiceDiscoveryListener.class).annotatedWith(UniqueAnnotations.create()).toInstance(new SamServiceDiscoveryListener() {
                    @Override
                    public void serviceInstanceCreated(SIURL url, ServiceKey contract) {
                        System.out.println("serviceCreated: url[" + url + "], contract [" + contract + "]");
                    }
                });
            }
        }).architecture(new SeeTestArchitecture()).implementationPackage(new TestImplementationDeclaration());
    }

    public static SEEConfiguration createSEEConfiguration(int port, SEEServiceSetupAction setupAction) {
        return createArchitectureConfiguration().setupAction(setupAction).bindToAddress(new InetSocketAddress(port));
    }
}
