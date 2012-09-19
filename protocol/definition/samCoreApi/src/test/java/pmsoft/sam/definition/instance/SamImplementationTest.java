package pmsoft.sam.definition.instance;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.definition.service.SamServiceDefinition;

import com.google.inject.AbstractModule;

public class SamImplementationTest {

	static class FakeServiceDefinitionImpl extends AbstractSamServiceDefinition {
		@Override
		public void loadServiceDefinition() {
		}
	}

	static class FakeServiceDefinitionExternalOne extends AbstractSamServiceDefinition {
		@Override
		public void loadServiceDefinition() {
		}
	}

	static class FakeServiceDefinitionExternalTwo extends AbstractSamServiceDefinition {
		@Override
		public void loadServiceDefinition() {
		}
	}

	static class FakeServiceModule extends AbstractModule {
		@Override
		protected void configure() {

		}
	}

	static class TestImplementation extends AbstractSamServiceImplementationContract {

		@SuppressWarnings("unchecked")
		public TestImplementation() {
			super(FakeServiceDefinitionImpl.class, FakeServiceModule.class, 
					(Class<? extends SamServiceDefinition>[]) new Class<?>[]{FakeServiceDefinitionExternalOne.class, FakeServiceDefinitionExternalTwo.class});
		}

	}

	@Test
	public void simpleServiceImplementationDefinition() {
		SamServiceImplementationContractLoader reader = Mockito.mock(SamServiceImplementationContractLoader.class);
		TestImplementation test = new TestImplementation();
		test.readContract(reader);
		//TODO
		
		
	}
}
