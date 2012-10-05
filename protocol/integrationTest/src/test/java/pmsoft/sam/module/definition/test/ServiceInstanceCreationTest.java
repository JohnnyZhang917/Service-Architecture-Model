package pmsoft.sam.module.definition.test;


public class ServiceInstanceCreationTest {
//	@Rule
//	public GuiceBerryRule guiceBerry = new GuiceBerryRule(SamProtoTestModule.class);
//
//	@Inject
//	private SamArchitecture architectureService;
//
//	@Inject
//	private SamServiceRegistryDeprecated serviceRegistry;
//
//	@Inject
//	private ServiceExecutionEnviroment see;
//
//	@Test
//	public void testServiceCreation() {
//		// Register implementations
//		serviceRegistry.registerServiceImplementation(new ServiceImplementationPackageA());
//
//		// Get implementations by service definition
//		SamArchitecture architecture = architectureService;
//		SamCategory userCat = architecture.getCategory("UserCategory");
//		Set<SamService> userServices = userCat.getDefinedServices();
//
//		Closure1<SamService> lookForServiceImplementation = closure(SamService.class);
//		{
//			of(serviceRegistry).getImplementationsForSpecification(var(SamService.class).getServiceKey());
//		}
//		List<ServiceImplementationKey> implementations = flatten(lookForServiceImplementation.each(userServices));
//
//		Closure1<ServiceImplementationKey> createServiceInstance = closure(ServiceImplementationKey.class);
//		{
//			of(see).executeServiceInstance(var(ServiceImplementationKey.class));
//		}
//
//		@SuppressWarnings("unchecked")
//		List<SIID> serviceInstanceId = (List<SIID>) createServiceInstance.each(implementations);
//		assertTrue(serviceInstanceId.size()>0);
//		for (SIID siid : serviceInstanceId) {
//			assertNotNull(siid);
//		}
//		
//		// Get a service Instance for each implementation
//		assertEquals(serviceInstanceId.size(), implementations.size());
//		
//		// Get the instance of service1
//		SamService service1def = architecture.getService(new ServiceKey(Service1Definition.class.getCanonicalName()));
//		List<ServiceInstance> instance = see.getInstanceForService(service1def);
//		assertTrue(instance.size()>0);
//		// The provided test implementations return the signature of the implementation key
//		Set<String> instanceDiffCheck = Sets.newHashSet();
//		for (ServiceInstance serviceInstance : instance) {
//			Injector sinjector = serviceInstance.getInjector();
//			Service1a serviceApi = sinjector.getInstance(Service1a.class);
//			String nameApi = serviceApi.getImplementationName();
////			System.out.println(nameApi);
//			ServiceImplementationKey implementationKey =  serviceInstance.getKey().getImplementationKey();
//			assertEquals(implementationKey.getServiceImplementationSignature(), nameApi);
//			assertTrue("Name implementation repeated. Test data don't allow this",instanceDiffCheck.add(nameApi));
//		}
//	}
}
