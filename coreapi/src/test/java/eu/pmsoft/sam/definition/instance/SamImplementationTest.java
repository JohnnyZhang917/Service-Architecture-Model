package eu.pmsoft.sam.definition.instance;

import com.google.inject.AbstractModule;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.testng.annotations.Test;
import eu.pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import eu.pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationLoader;
import eu.pmsoft.sam.definition.implementation.SamServicePackageLoader;
import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

/**
 * Tests to check the loading calls mapping and to feel the client API
 *
 * @author pawel
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

    static class FakeServiceImplementation extends AbstractSamServiceImplementationDefinition<FakeServiceDefinitionImpl> {

        protected FakeServiceImplementation() {
            super(FakeServiceDefinitionImpl.class);
        }

        @Override
        protected void implementationDefinition() {
            withBindingsTo(FakeServiceDefinitionExternalOne.class);
            withBindingsTo(FakeServiceDefinitionExternalTwo.class);
            implementedInModule(FakeServiceModule.class);
        }
    }

    static class TestImplementation extends AbstractSamImplementationPackage {

        @Override
        public void packageDefinition() {
            registerImplementation(new FakeServiceImplementation());
        }
    }

    @Test
    public void simpleServicePackageDefinition() {
        TestImplementation test = new TestImplementation();
        SamServicePackageLoader mockReader = Mockito.mock(SamServicePackageLoader.class);
        test.loadContractPackage(mockReader);
        verify(mockReader).registerImplementation(any(FakeServiceImplementation.class));
    }

    @Test
    public void simpleServiceImplementationDefinition() {
        SamServiceImplementationLoader mockLoader = Mockito.mock(SamServiceImplementationLoader.class);
        final SamServiceImplementationLoader.InternalServiceImplementationDefinition mockInternalDefinition = Mockito.mock(SamServiceImplementationLoader.InternalServiceImplementationDefinition.class);
        Stubber stubber = Mockito.doAnswer(new Answer<SamServiceImplementationLoader.InternalServiceImplementationDefinition>() {
            @Override
            public SamServiceImplementationLoader.InternalServiceImplementationDefinition answer(InvocationOnMock invocation) throws Throwable {
                return mockInternalDefinition;
            }
        });
        stubber.when(mockLoader).provideContract(FakeServiceDefinitionImpl.class);
//        stubber.when(mockInternalDefinition).withBindingsTo(any(Class.class));

        FakeServiceImplementation implementationDefinition = new FakeServiceImplementation();
        implementationDefinition.loadServiceImplementationDefinition(mockLoader);

        verify(mockLoader).provideContract(FakeServiceDefinitionImpl.class);
        verify(mockInternalDefinition).withBindingsTo(FakeServiceDefinitionExternalOne.class);
        verify(mockInternalDefinition).withBindingsTo(FakeServiceDefinitionExternalTwo.class);
        verify(mockInternalDefinition).implementedInModule(FakeServiceModule.class);
    }

}
