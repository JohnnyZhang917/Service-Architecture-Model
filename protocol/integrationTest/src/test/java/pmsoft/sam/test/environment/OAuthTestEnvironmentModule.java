package pmsoft.sam.test.environment;

import pmsoft.sam.module.definition.test.oauth.OAuthArchitectureModule;

import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;

import deprecated.pmsoft.sam.module.canonical.CanonicalProtocolInfrastructure;
import deprecated.pmsoft.sam.module.see.local.SEELocalModule;
import deprecated.pmsoft.sam.module.serviceRegistry.local.LocalVMServiceRegistryModule;

public class OAuthTestEnvironmentModule extends GuiceBerryModule {
	@Override
	protected void configure() {
		super.configure();
		bind(GuiceBerryEnvMain.class).to(ProtoEnviromentStarter.class);
		// Service Architecture Instance
		install(new OAuthArchitectureModule());

		// ServiceRegistry instance
		install(new LocalVMServiceRegistryModule());

		// SEE 
		install(new SEELocalModule());
		
		install(new CanonicalProtocolInfrastructure());
		
		// request explicit binding for the prototype
		binder().requireExplicitBindings();
	}
	
	static class ProtoEnviromentStarter implements GuiceBerryEnvMain {
		public void run() {
		}
		
	}
}
