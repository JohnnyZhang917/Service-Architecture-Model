package eu.pmsoft.sam.definition.service;

import com.google.inject.name.Names;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;

public class SamServiceDefinitionTest {

    static interface AnyInterface1 {
    }

    static interface AnyInterface2 {
    }

    static class TestServiceDefinition extends AbstractSamServiceDefinition {
        @Override
        public void loadServiceDefinition() {
            withKey(AnyInterface1.class);
            withKey(AnyInterface2.class);
            withKey(AnyInterface1.class, Names.named("test1"));
            withKey(AnyInterface2.class, Names.named("test2"));
        }
    }

    @Test
    public void testServiceDefinitionReader() {
        SamServiceDefinitionLoader loader = Mockito.mock(SamServiceDefinitionLoader.class);
        SamServiceDefinitionLoader.SamServiceDefinitionInterfacesLoader lmock = Mockito.mock(SamServiceDefinitionLoader.SamServiceDefinitionInterfacesLoader.class);
        Mockito.when(loader.definedIn(Matchers.<Class<? extends SamServiceDefinition>>any())).thenReturn(lmock);
        TestServiceDefinition definition = new TestServiceDefinition();
        definition.loadServiceDefinition(loader);
        verify(loader).definedIn(TestServiceDefinition.class);
        verify(lmock).withKey(AnyInterface1.class);
        verify(lmock).withKey(AnyInterface2.class);
        verify(lmock).withKey(AnyInterface1.class, Names.named("test1"));
        verify(lmock).withKey(AnyInterface2.class, Names.named("test2"));
    }


}
