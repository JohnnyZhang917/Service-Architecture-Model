package pmsoft.sam.module.definition.test.data.impl.c;

import pmsoft.sam.module.definition.implementation.AbstractSamServiceImplementationDefinition;
import pmsoft.sam.module.definition.test.data.service.Service3Definition;
import pmsoft.sam.module.definition.test.data.service.Service4Definition;

public class ServiceImplementationPackageC extends AbstractSamServiceImplementationDefinition {

	@Override
	public void implementationDefinition() {
		registerImplementationOf(Service3Definition.class).givenByModule(Service3ImplPackC.class)
				.accessTo(Service4Definition.class).done();
		registerImplementationOf(Service4Definition.class).givenByModule(Service4ImplPackC.class)
				.accessTo(Service3Definition.class).done();
		registerImplementationOf(Service4Definition.class).givenByModule(Service4ImplPackCPrototype.class).done();
	}
}
