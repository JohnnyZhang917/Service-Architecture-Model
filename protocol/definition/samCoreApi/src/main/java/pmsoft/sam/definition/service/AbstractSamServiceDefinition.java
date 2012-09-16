package pmsoft.sam.definition.service;

public abstract class AbstractSamServiceDefinition implements
		SamServiceDefinition {

	private SamServiceLoader loaderRef;

	public void loadServiceDefinition(SamServiceLoader loader) {
		try {
			loader.setupLoadContext(this.getClass());
			this.loaderRef = loader;
			
			loadServiceDefinition();
		} finally {
			this.loaderRef = null;
		}
	}

	abstract public void loadServiceDefinition();

	protected void addInterface(Class<?> interfaceReference) {
		loaderRef.addInterface(interfaceReference);
	}
}
