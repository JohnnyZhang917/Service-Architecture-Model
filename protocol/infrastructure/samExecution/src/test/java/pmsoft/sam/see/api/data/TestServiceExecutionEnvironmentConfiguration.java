package pmsoft.sam.see.api.data;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.SEEConfiguration;
import pmsoft.sam.see.SEEConfigurationBuilder;
import pmsoft.sam.see.SEEServiceSetupAction;
import pmsoft.sam.see.api.data.architecture.SeeTestArchitecture;
import pmsoft.sam.see.api.data.impl.TestImplementationDeclaration;
import pmsoft.sam.see.api.data.impl.TestServiceOneModule;
import pmsoft.sam.see.api.data.impl.TestServiceTwoModule;
import pmsoft.sam.see.api.data.impl.TestServiceZeroModule;
import pmsoft.sam.see.api.data.transactions.TestTransactionDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;
import pmsoft.sam.see.api.model.SamInstanceTransaction;
import pmsoft.sam.see.api.plugin.SamServiceDiscoveryListener;
import pmsoft.sam.see.api.transaction.SamInjectionConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.internal.UniqueAnnotations;

public class TestServiceExecutionEnvironmentConfiguration {

	public static SEEConfiguration createTestServerConfiguration(int port) {
		return createArchitectureConfiguration().setupAction(new SEEServiceSetupAction() {

			@Override
			public void setup() {
				SIID zero = createServiceInstance(TestServiceZeroModule.class);
				SIID one = createServiceInstance(TestServiceOneModule.class);
				SIID two = createServiceInstance(TestServiceTwoModule.class);

				SamInjectionConfiguration zeroSingle = TestTransactionDefinition.createServiceZeroConfiguration(zero);
				SamInstanceTransaction transactionZero = setupServiceTransaction(zeroSingle);
				transactionZero.getTransactionURL();
				
				SamInjectionConfiguration oneSingle = TestTransactionDefinition.createServiceOneConfiguration(one);
				SamInstanceTransaction transactionOne = setupServiceTransaction(oneSingle);
				transactionOne.getTransactionURL();

				SamInjectionConfiguration twoLocal = TestTransactionDefinition.createServiceTwoConfiguration(two, one, zero);
				SamInstanceTransaction transactionTwo = setupServiceTransaction(twoLocal);
				transactionTwo.getTransactionURL();

			}
		}).bindToPort(port);
	}

	public static SEEConfiguration createTestClientConfiguration(int port, final SIURL externalServiceInstanceOneURL, final SIURL externalServiceInstanceZeroURL) {
		return createArchitectureConfiguration().setupAction(new SEEServiceSetupAction() {
			
			@Override
			public void setup() {

				SIID two = createServiceInstance(TestServiceTwoModule.class);
				SamInjectionConfiguration twoRemote = TestTransactionDefinition.createServiceTwoConfiguration(two, externalServiceInstanceOneURL,externalServiceInstanceZeroURL);
				setupServiceTransaction(twoRemote);
				
			}
		}).bindToPort(port);
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
