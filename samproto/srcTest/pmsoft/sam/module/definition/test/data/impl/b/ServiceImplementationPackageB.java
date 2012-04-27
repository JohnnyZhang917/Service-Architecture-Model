package pmsoft.sam.module.definition.test.data.impl.b;

import pmsoft.sam.module.definition.implementation.AbstractSamServiceImplementationDefinition;
import pmsoft.sam.module.definition.test.data.service.Service1Definition;
import pmsoft.sam.module.definition.test.data.service.Service2Definition;

public class ServiceImplementationPackageB extends AbstractSamServiceImplementationDefinition {

	@Override
	public void implementationDefinition() {
		registerImplementationOf(Service1Definition.class).givenByModule(Service1ImplPackB.class)
				.accessTo(Service2Definition.class).done();
		registerImplementationOf(Service2Definition.class).givenByModule(Service2ImplPackBalpha.class).done();
		registerImplementationOf(Service2Definition.class).givenByModule(Service2ImplPackBbeta.class).done();
	}

}
