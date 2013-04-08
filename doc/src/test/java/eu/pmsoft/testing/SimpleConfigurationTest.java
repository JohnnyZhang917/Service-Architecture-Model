package eu.pmsoft.testing;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

@Guice(modules = {SimpleConfiguration.class, NestedModuleOne.class, NestedModuleTwo.class})
public class SimpleConfigurationTest {
    @Inject
    private Injector injector;

    @Test
    public void testExposedKeys() {
        assertNotNull(injector.getExistingBinding(Key.get(ClientApi.class)));
        assertNull(injector.getExistingBinding(Key.get(ClientInternalApi.class)));
    }

    @Inject
    private ClientApi api;

    @Test
    public void testInjectedLoggers() {
        api.testLogging();
    }
}
