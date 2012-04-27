package pmsoft.sam.module.definition.test.data.impl.a;

import pmsoft.sam.module.definition.implementation.AbstractSamServiceImplementationDefinition;
import pmsoft.sam.module.definition.test.data.service.Service1Definition;

public class ServiceImplementationPackageA extends AbstractSamServiceImplementationDefinition {

	@Override
	public void implementationDefinition() {
//		serviceImplementation(new ServiceKey(Service1Definition.class), Service1ImplA.class);
//		serviceImplementation(Service1Definition.class, Service1ImplB.class);
//		serviceImplementation(Service1Definition.class, Service1ImplC.class);
		registerImplementationOf(Service1Definition.class).givenByModule(Service1ImplA.class).done();
		registerImplementationOf(Service1Definition.class).givenByModule(Service1ImplB.class).done();
		registerImplementationOf(Service1Definition.class).givenByModule(Service1ImplC.class).done();
	}

}
