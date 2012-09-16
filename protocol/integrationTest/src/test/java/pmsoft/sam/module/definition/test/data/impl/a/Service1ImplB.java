package pmsoft.sam.module.definition.test.data.impl.a;

import pmsoft.sam.module.definition.test.data.service.Service1a;

import com.google.inject.AbstractModule;

public class Service1ImplB extends AbstractModule {

	public static class Service1LocalImple implements Service1a {

		public String getImplementationName() {
			return Service1ImplB.class.getCanonicalName();
		}
		public boolean testServiceInteraction() {
			return true;
		}
	}
	
	@Override
	protected void configure() {
		bind(Service1a.class).to(Service1LocalImple.class);
	}

}
