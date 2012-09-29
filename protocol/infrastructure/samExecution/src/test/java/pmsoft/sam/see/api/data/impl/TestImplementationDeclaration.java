package pmsoft.sam.see.api.data.impl;

import pmsoft.sam.definition.implementation.AbstractSamImplementationPackage;
import pmsoft.sam.see.api.data.architecture.TestServiceOne;
import pmsoft.sam.see.api.data.architecture.TestServiceTwo;

public class TestImplementationDeclaration extends AbstractSamImplementationPackage {

	@Override
	public void implementationDefinition() {
		provideContract(TestServiceOne.class)
				.implementedInModule(TestServiceOneModule.class);

		provideContract(TestServiceTwo.class)
				.withBindingsTo(TestServiceOne.class)
				.implementedInModule(TestServiceTwoModule.class);
	}
	
}
