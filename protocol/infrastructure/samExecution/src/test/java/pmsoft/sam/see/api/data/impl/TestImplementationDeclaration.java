package pmsoft.sam.see.api.data.impl;

import pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import pmsoft.sam.see.api.data.architecture.service.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.service.TestServiceTwo;
import pmsoft.sam.see.api.data.architecture.service.TestServiceZero;

public class TestImplementationDeclaration extends AbstractSamImplementationPackage {

	@Override
	public void implementationDefinition() {
		provideContract(TestServiceZero.class).implementedInModule(TestServiceZeroModule.class);

		provideContract(TestServiceOne.class).implementedInModule(TestServiceOneModule.class);

		provideContract(TestServiceTwo.class).withBindingsTo(TestServiceOne.class).withBindingsTo(TestServiceZero.class)
				.implementedInModule(TestServiceTwoModule.class);

	}

}
