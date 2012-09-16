package pmsoft.sam.module.definition.test.data.impl.b;

import pmsoft.sam.module.definition.test.data.service.Service1a;
import pmsoft.sam.module.definition.test.data.service.Service2a;
import pmsoft.sam.module.definition.test.data.service.Service2b;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class Service1ImplPackB extends AbstractModule {

	public static class Service1LocalImple implements Service1a {

		@Inject
		private Service2a service2aRef;

		@Inject
		private Service2b service2bRef;
		
		public String getImplementationName() {
			return Service1ImplPackB.class.getCanonicalName();
		}

		public boolean testServiceInteraction() {
			if(!service2aRef.checkConnection()){
				return false;
			}
			return service2bRef.isThisYourName(service2aRef.getServiceName());
		}
	}
	
	@Override
	protected void configure() {
		bind(Service1a.class).to(Service1LocalImple.class);
	}

}
