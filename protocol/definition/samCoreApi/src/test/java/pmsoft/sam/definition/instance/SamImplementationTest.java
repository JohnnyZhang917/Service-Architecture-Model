package pmsoft.sam.definition.instance;

import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.testng.annotations.Test;

import pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import pmsoft.sam.definition.implementation.SamServiceImplementationContractLoader;
import pmsoft.sam.definition.implementation.SamServiceImplementationContractLoader.SamServiceImplementationGrammarContract;
import pmsoft.sam.definition.service.AbstractSamServiceDefinition;

import com.google.inject.AbstractModule;
/**
 * Tests to check the loading calls mapping and to feel the client API
 * @author pawel
 *
 */
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

	static class TestImplementation extends AbstractSamImplementationPackage {

		@Override
		public void implementationDefinition() {
			provideContract(FakeServiceDefinitionImpl.class)
				.withBindingsTo(FakeServiceDefinitionExternalOne.class)
				.withBindingsTo(FakeServiceDefinitionExternalTwo.class)
				.implementedInModule(FakeServiceModule.class);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void simpleServiceImplementationDefinition() {
		// Nested mocks for a 2 level regular grammar, no better solution right now.
		SamServiceImplementationContractLoader reader = Mockito.mock(SamServiceImplementationContractLoader.class);
		final SamServiceImplementationGrammarContract contract = Mockito.mock(SamServiceImplementationGrammarContract.class);
		Stubber chaining = Mockito.doAnswer(new Answer<SamServiceImplementationGrammarContract>() {
			@Override
			public SamServiceImplementationGrammarContract answer(
					InvocationOnMock invocation) throws Throwable {
				return contract;
			}
		});
		chaining.when(reader).provideContract(any(Class.class));
		chaining.when(contract).withBindingsTo(any(Class.class));
		
		TestImplementation test = new TestImplementation();
		test.loadContractPackage(reader);
		verify(reader).provideContract(FakeServiceDefinitionImpl.class);
		verify(contract).withBindingsTo(FakeServiceDefinitionExternalOne.class);
		verify(contract).withBindingsTo(FakeServiceDefinitionExternalTwo.class);
		verify(contract).implementedInModule(FakeServiceModule.class);
		
		
		
	}
}
