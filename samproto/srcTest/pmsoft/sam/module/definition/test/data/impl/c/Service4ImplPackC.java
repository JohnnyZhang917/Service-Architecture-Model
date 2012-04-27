package pmsoft.sam.module.definition.test.data.impl.c;

import pmsoft.sam.module.definition.test.data.service.Service3a;
import pmsoft.sam.module.definition.test.data.service.Service4a;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class Service4ImplPackC extends AbstractModule {

	private static class Service4aImpl implements Service4a {

		@Inject
		private Service3a nextService;

		public boolean executeNextNestedService(int counter) {
			if (counter == 0) {
				System.out.println("S4: counter == 0, true");
				return true;
			}
			System.out.println("S4: counter == " + counter + ", true");
			return nextService.executeNestedService(--counter);
		}

	}

	@Override
	protected void configure() {
		bind(Service4a.class).to(Service4aImpl.class);
	}

}
