package pmsoft.sam.module.definition.test.data.impl.c;

import pmsoft.sam.module.definition.test.data.service.Service3a;
import pmsoft.sam.module.definition.test.data.service.Service4a;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class Service3ImplPackC extends AbstractModule {

	private static class Service3aImpl implements Service3a {

		@Inject
		private Service4a nextService;

		public boolean executeNestedService(int counter) {
			if (counter == 0) {
				System.out.println("S3: counter == 0, true");
				return true;
			}
			System.out.println("S3: counter == "+counter+", true");
			return nextService.executeNextNestedService(--counter);
		}

	}

	@Override
	protected void configure() {
		bind(Service3a.class).to(Service3aImpl.class);
	}

}
