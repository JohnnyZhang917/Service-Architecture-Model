package pmsoft.sam.module.definition.test;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Rule;
import org.junit.Test;

import pmsoft.sam.module.definition.test.oauth.impl.OAuthServicesImplementationPackage;
import pmsoft.sam.module.definition.test.oauth.impl.beppa.BeppaPrintingPhotoService;
import pmsoft.sam.module.definition.test.oauth.impl.faji.FajiPhotoSharingService;
import pmsoft.sam.module.definition.test.oauth.impl.jane.JaneGuiServiceModule;
import pmsoft.sam.module.definition.test.oauth.service.JaneGuiService;
import pmsoft.sam.module.definition.test.oauth.service.JaneGuiServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PhotoSharingServiceDefinition;
import pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoServiceDefinition;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.serviceRegistry.SamServiceRegistryDeprecated;
import pmsoft.sam.module.see.transaction.InjectionConfiguration;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.ServiceImplementationKey;
import pmsoft.sam.test.environment.OAuthTestEnvironmentModule;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

public class OAuthPrintingPhotoTest {
	@Rule
	public GuiceBerryRule guiceBerry = new GuiceBerryRule(OAuthTestEnvironmentModule.class);

	@Inject
	private SamServiceRegistryDeprecated serviceRegistry;

	@Inject
	private ServiceExecutionEnviroment see;

	@Test
	public void testOAuthInteraction() {
		// Register implementations
		serviceRegistry.registerServiceImplementation(new OAuthServicesImplementationPackage());

		// this are the reference to the existing implementations of the
		// services
		ServiceImplementationKey fajiImplementationDefinition = new ServiceImplementationKey(
				PrintingPhotoServiceDefinition.class, FajiPhotoSharingService.class);
		ServiceImplementationKey beppaImplementationDefinition = new ServiceImplementationKey(
				PhotoSharingServiceDefinition.class, BeppaPrintingPhotoService.class);

		ServiceImplementationKey janeGuiImplementationDefinition = new ServiceImplementationKey(JaneGuiServiceDefinition.class,
				JaneGuiServiceModule.class);

		// Now create a runnign service instance for each service

		SIID faji = see.executeServiceInstance(fajiImplementationDefinition);
		SIID beppa = see.executeServiceInstance(beppaImplementationDefinition);

		SIID janeGuiInterface = see.executeServiceInstance(janeGuiImplementationDefinition);

		// Create alpha transaction and run
		System.out.println("SEE: Jane start the OAuth test");
		System.out.println("SEE: Setup transactions to run the service interaction");

		TransactionConfigurator fajiTransaction = see.createTransactionConfiguration(faji);
		SamTransaction fajiReference = fajiTransaction.createTransactionContext();
		URL fajiURL = fajiReference.getTransactionURL();

		TransactionConfigurator beppaTransaction = see.createTransactionConfiguration(beppa);
		SamTransaction beppaReference = beppaTransaction.createTransactionContext();
		URL beppaURL = beppaReference.getTransactionURL();

		TransactionConfigurator transaction = see.createTransactionConfiguration(janeGuiInterface);
		InjectionConfiguration configuration = transaction.getInjectionConfiguration();
		configuration.bindExternalInstance(fajiImplementationDefinition.getServiceSpecificationKey(), fajiURL);
		configuration.bindExternalInstance(beppaImplementationDefinition.getServiceSpecificationKey(), beppaURL);

		SamTransaction janeGui = transaction.createTransactionContext();
		System.out.println("SEE: Execute Jane interaction");
		boolean result = janeGui.getInjector().getInstance(JaneGuiService.class).runPrintingPhotoTest();
		assertTrue("Jane is not happy with the printing result!!", result);

	}

}
