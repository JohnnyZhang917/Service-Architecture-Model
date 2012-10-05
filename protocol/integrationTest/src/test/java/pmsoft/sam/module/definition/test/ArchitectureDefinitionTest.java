package pmsoft.sam.module.definition.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.SamCategory;
import pmsoft.sam.test.environment.SamProtoTestModule;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

public class ArchitectureDefinitionTest {
	@Rule
	public GuiceBerryRule guiceBerry = new GuiceBerryRule(SamProtoTestModule.class);

	@Inject
	private SamArchitecture architectureService;

//	@Inject
//	private SamServiceRegistryDeprecated serviceRegistry;
//
	@Test
	public void testArchitectureDefinition() {
		SamArchitecture architecture = architectureService;
		assertEquals(4, architecture.getAllService().size());
		assertEquals(2, architecture.getAllCategories().size());
		
		SamCategory userCat = architecture.getCategory("UserCategory");
		assertEquals(1, userCat.getAccessibleCategories().size());
		assertEquals(1, userCat.getDefinedServices().size());

		SamCategory dataCat = architecture.getCategory("DataCategory");
		assertEquals(1, dataCat.getAccessibleCategories().size());
		assertEquals(3, dataCat.getDefinedServices().size());
		assertTrue(true);
	}

	@Test
	public void testServiceRegistry() {
//		serviceRegistry.registerServiceImplementation(new ServiceImplementationPackageA());
//		
//		ServiceKey service1key = new ServiceKey(Service1Definition.class);
//		List<ServiceImplementationKey> registered = serviceRegistry.getImplementationsForSpecification(service1key);
//		assertEquals(3, registered.size());
//
//		SamArchitecture architecture = architectureService;
//		assertEquals(4, architecture.getAllService().size());
//		assertEquals(2, architecture.getAllCategories().size());
	}
	
	

}
