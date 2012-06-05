package doc.wjug.graph;

import com.google.inject.AbstractModule;

public class GraphModuleSimple extends AbstractModule {

	@Override
	protected void configure() {
		bind(ServiceInterface.class).to(ServiceImplementation.class);
		bind(ExternalServiceInterface.class).to(ExternalServiceInstance.class);
	}

}
