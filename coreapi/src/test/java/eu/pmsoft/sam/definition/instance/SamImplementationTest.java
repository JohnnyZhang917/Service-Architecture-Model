/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.sam.definition.instance;

import com.google.inject.AbstractModule;
import eu.pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import eu.pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationDefinitionLoader;
import eu.pmsoft.sam.definition.implementation.SamServicePackageLoader;
import eu.pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.testng.annotations.Test;

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
            super(FakeServiceDefinitionImpl.class, FakeServiceModule.class);
        }

        @Override
        protected void implementationDefinition() {
            withBindingsTo(FakeServiceDefinitionExternalOne.class);
            withBindingsTo(FakeServiceDefinitionExternalTwo.class);
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
        SamServiceImplementationDefinitionLoader mockLoader = Mockito.mock(SamServiceImplementationDefinitionLoader.class);
        final SamServiceImplementationDefinitionLoader.ContractAndModule mockInternalDefinition = Mockito.mock(SamServiceImplementationDefinitionLoader.ContractAndModule.class);

        Stubber stubber = Mockito.doAnswer(new Answer<SamServiceImplementationDefinitionLoader.ContractAndModule>() {
            @Override
            public SamServiceImplementationDefinitionLoader.ContractAndModule answer(InvocationOnMock invocation) throws Throwable {
                return mockInternalDefinition;
            }
        });
        stubber.when(mockLoader).signature(FakeServiceDefinitionImpl.class, FakeServiceModule.class);

        FakeServiceImplementation implementationDefinition = new FakeServiceImplementation();
        implementationDefinition.loadServiceImplementationDefinition(mockLoader);

        verify(mockLoader).signature(FakeServiceDefinitionImpl.class, FakeServiceModule.class);
        verify(mockInternalDefinition).withBindingsTo(FakeServiceDefinitionExternalOne.class);
        verify(mockInternalDefinition).withBindingsTo(FakeServiceDefinitionExternalTwo.class);
    }

}
