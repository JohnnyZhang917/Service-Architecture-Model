package eu.pmsoft.sam.definition.service;

import com.google.inject.name.Names;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SamServiceDefinitionTest {

    static interface AnyInterface1 {
    }

    static interface AnyInterface2 {
    }

    static class TestServiceDefinition extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            addInterface(AnyInterface1.class);
            addInterface(AnyInterface2.class);
            addInterface(AnyInterface1.class, Names.named("test1"));
            addInterface(AnyInterface2.class, Names.named("test2"));
        }
    }

    @Test
    public void testServiceDefinitionReader() {
        SamServiceLoaderBase loader = Mockito.mock(SamServiceLoaderBase.class);
        SamServiceLoaderBase.SamServiceLoader lmock= Mockito.mock(SamServiceLoaderBase.SamServiceLoader.class);
        Mockito.when(loader.setupLoadContext(Matchers.<Class<? extends SamServiceDefinition>>any())).thenReturn(lmock);
        TestServiceDefinition definition = new TestServiceDefinition();
        definition.loadServiceDefinition(loader);
        verify(loader).setupLoadContext(TestServiceDefinition.class);
        verify(lmock).addInterface(AnyInterface1.class);
        verify(lmock).addInterface(AnyInterface2.class);
        verify(lmock).addInterface(AnyInterface1.class, Names.named("test1"));
        verify(lmock).addInterface(AnyInterface2.class, Names.named("test2"));
    }


}
