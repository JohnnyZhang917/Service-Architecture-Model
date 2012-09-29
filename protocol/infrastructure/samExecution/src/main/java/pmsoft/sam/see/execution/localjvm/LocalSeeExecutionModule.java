package pmsoft.sam.see.execution.localjvm;

import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.SamServiceRegistry;

import com.google.inject.AbstractModule;

public class LocalSeeExecutionModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SamServiceRegistry.class).to(SamServiceRegistryLocal.class).asEagerSingleton();
		bind(SamExecutionNode.class).to(SamExecutionNodeJVM.class).asEagerSingleton();
	}

}
