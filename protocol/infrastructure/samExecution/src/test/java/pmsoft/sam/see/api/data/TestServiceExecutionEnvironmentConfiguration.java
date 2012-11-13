package pmsoft.sam.see.api.data;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.SEEConfiguration;
import pmsoft.sam.see.SEEConfigurationBuilder;
import pmsoft.sam.see.api.data.architecture.SeeTestArchitecture;
import pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.plugin.SamServiceDiscoveryListener;

import com.google.inject.AbstractModule;
import com.google.inject.internal.UniqueAnnotations;

public class TestServiceExecutionEnvironmentConfiguration {

	public static SEEConfiguration createSEEConfiguration(int port) {
		return createArchitectureConfiguration().bindToPort(port);
	}
	
	private static SEEConfigurationBuilder.SEEConfigurationGrammar createArchitectureConfiguration() {
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
	
}
