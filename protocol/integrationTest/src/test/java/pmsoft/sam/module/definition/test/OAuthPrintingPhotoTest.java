package pmsoft.sam.module.definition.test;


public class OAuthPrintingPhotoTest {
//	@Rule
//	public GuiceBerryRule guiceBerry = new GuiceBerryRule(OAuthTestEnvironmentModule.class);
//
//	@Inject
//	private SamServiceRegistryDeprecated serviceRegistry;
//
//	@Inject
//	private ServiceExecutionEnviroment see;
//
//	@Test
//	public void testOAuthInteraction() {
//		// Register implementations
//		serviceRegistry.registerServiceImplementation(new OAuthServicesImplementationPackage());
//
//		// this are the reference to the existing implementations of the
//		// services
//		ServiceImplementationKey fajiImplementationDefinition = new ServiceImplementationKey(
//				PrintingPhotoServiceDefinition.class, FajiPhotoSharingService.class);
//		ServiceImplementationKey beppaImplementationDefinition = new ServiceImplementationKey(
//				PhotoSharingServiceDefinition.class, BeppaPrintingPhotoService.class);
//
//		ServiceImplementationKey janeGuiImplementationDefinition = new ServiceImplementationKey(JaneGuiServiceDefinition.class,
//				JaneGuiServiceModule.class);
//
//		// Now create a runnign service instance for each service
//
//		SIID faji = see.executeServiceInstance(fajiImplementationDefinition);
//		SIID beppa = see.executeServiceInstance(beppaImplementationDefinition);
//
//		SIID janeGuiInterface = see.executeServiceInstance(janeGuiImplementationDefinition);
//
//		// Create alpha transaction and run
//		System.out.println("SEE: Jane start the OAuth test");
//		System.out.println("SEE: Setup transactions to run the service interaction");
//
//		TransactionConfigurator fajiTransaction = see.createTransactionConfiguration(faji);
//		SamTransaction fajiReference = fajiTransaction.createTransactionContext();
//		URL fajiURL = fajiReference.getTransactionURL();
//
//		TransactionConfigurator beppaTransaction = see.createTransactionConfiguration(beppa);
//		SamTransaction beppaReference = beppaTransaction.createTransactionContext();
//		URL beppaURL = beppaReference.getTransactionURL();
//
//		TransactionConfigurator transaction = see.createTransactionConfiguration(janeGuiInterface);
//		InjectionConfiguration configuration = transaction.getInjectionConfiguration();
//		configuration.bindExternalInstance(fajiImplementationDefinition.getServiceSpecificationKey(), fajiURL);
//		configuration.bindExternalInstance(beppaImplementationDefinition.getServiceSpecificationKey(), beppaURL);
//
//		SamTransaction janeGui = transaction.createTransactionContext();
//		System.out.println("SEE: Execute Jane interaction");
//		boolean result = janeGui.getInjector().getInstance(JaneGuiService.class).runPrintingPhotoTest();
//		assertTrue("Jane is not happy with the printing result!!", result);
//
//	}
//
}
