package pmsoft.sam.definition.service;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class SamServiceDefinitionTest {

	static interface AnyInterface1 {}
	static interface AnyInterface2 {}
	
	static class TestServiceDefinition extends AbstractSamServiceDefinition {
		@Override
		public void loadServiceDefinition() {
			addInterface(AnyInterface1.class);
			addInterface(AnyInterface2.class);
		}
	}

	@Test
	public void testServiceDefinitionReader(){
		SamServiceLoader loader = Mockito.mock(SamServiceLoader.class);
		TestServiceDefinition definition = new TestServiceDefinition();
		definition.loadServiceDefinition(loader);
		verify(loader).setupLoadContext(TestServiceDefinition.class);
		verify(loader).addInterface(AnyInterface1.class);
		verify(loader).addInterface(AnyInterface2.class);
	}
	
	
	
}
