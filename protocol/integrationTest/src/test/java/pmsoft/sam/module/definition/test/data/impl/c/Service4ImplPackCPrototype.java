package pmsoft.sam.module.definition.test.data.impl.c;

import pmsoft.sam.module.definition.test.data.service.Service4a;

import com.google.inject.AbstractModule;

public class Service4ImplPackCPrototype extends AbstractModule {

	private static class Service4aImpl implements Service4a {

		public boolean executeNextNestedService(int counter) {
			if (counter == 0) {
				System.out.println("S4 final: counter == 0, true");
				return true;
			}
			System.out.println("S4 final: counter != 0, false");
			return false;
		}

	}

	@Override
	protected void configure() {
		bind(Service4a.class).to(Service4aImpl.class);
	}

}
