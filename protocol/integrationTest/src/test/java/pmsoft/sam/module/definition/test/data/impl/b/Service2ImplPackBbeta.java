package pmsoft.sam.module.definition.test.data.impl.b;

import pmsoft.sam.module.definition.test.data.service.Service2a;
import pmsoft.sam.module.definition.test.data.service.Service2b;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;

public class Service2ImplPackBbeta extends AbstractModule {

	public static class Service2aLocalImpl implements Service2a {

		public boolean checkConnection() {
			return true;
		}

		public String getServiceName() {
			return Service2ImplPackBbeta.class.getCanonicalName();
		}

	}

	public static class Service2bLocalImpl implements Service2b {

		public boolean isThisYourName(String name) {
			Preconditions.checkNotNull(name);
			System.out.println("beta");
			return name.compareTo(Service2ImplPackBbeta.class.getCanonicalName()) == 0;
		}

	}

	@Override
	protected void configure() {
		bind(Service2a.class).to(Service2aLocalImpl.class);
		bind(Service2b.class).to(Service2bLocalImpl.class);
	}

}
