package doc.wjug.graph;

import com.google.inject.Inject;

public class ServiceImplementation implements ServiceInterface{

	private final ExternalServiceInterface externalInstance;

	@Inject
	public ServiceImplementation(ExternalServiceInterface externalInstance) {
		super();
		this.externalInstance = externalInstance;
	}
	
	
}
