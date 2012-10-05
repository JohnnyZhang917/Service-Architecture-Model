package pmsoft.sam.see.execution.localjvm;

import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.SamServiceRegistry;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class LocalSeeExecutionModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SamExecutionNode.class).to(SamExecutionNodeJVM.class).asEagerSingleton();
	}

	@Provides
	public final SamServiceRegistry getSamServiceRegistry(SamExecutionNode executionNode) {
		return executionNode;
	}

}
