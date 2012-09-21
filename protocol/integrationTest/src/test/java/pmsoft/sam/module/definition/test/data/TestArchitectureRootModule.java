package pmsoft.sam.module.definition.test.data;

import pmsoft.sam.architecture.definition.SamArchitectureDefinition;

import com.google.inject.AbstractModule;

public class TestArchitectureRootModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SamArchitectureDefinition.class).to(TestArchitecture.class).asEagerSingleton();
	}

}
