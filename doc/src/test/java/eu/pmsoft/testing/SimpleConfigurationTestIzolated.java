package eu.pmsoft.testing;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Guice(modules = {SimpleConfiguration.class, SimpleConfigurationTestIzolated.LibsMockTestModule.class})
public class SimpleConfigurationTestIzolated {
    public static class LibsMockTestModule extends AbstractModule {
        static LibraryTwoApi twoApi = mock(LibraryTwoApi.class);
        static LibraryOneApi oneApi = mock(LibraryOneApi.class);

        @Override
        protected void configure() {
            bind(LibraryOneApi.class).toInstance(oneApi);
            bind(LibraryTwoApi.class).toInstance(twoApi);
        }
    }

    @Inject
    private ClientApi clientApi;

    @Test
    public void testMockingInjection() {
        clientApi.testLogging();
        verify(LibsMockTestModule.oneApi).oneApiCall();
        verify(LibsMockTestModule.twoApi).twoApiCall();
    }
}
