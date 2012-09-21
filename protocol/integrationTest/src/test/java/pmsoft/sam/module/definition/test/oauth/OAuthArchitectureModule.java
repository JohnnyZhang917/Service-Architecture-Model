package pmsoft.sam.module.definition.test.oauth;

import pmsoft.sam.architecture.definition.SamArchitectureDefinition;

import com.google.inject.AbstractModule;

public class OAuthArchitectureModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SamArchitectureDefinition.class).to(OAuthTestArchitecture.class).asEagerSingleton();
	}

}