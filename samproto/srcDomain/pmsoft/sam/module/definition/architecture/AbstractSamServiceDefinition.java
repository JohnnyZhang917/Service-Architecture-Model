package pmsoft.sam.module.definition.architecture;

import pmsoft.sam.module.definition.architecture.grammar.SamArchitectureLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamServiceLoader;

public abstract class AbstractSamServiceDefinition implements
		SamServiceDefinition {

	private SamArchitectureLoader loaderRef;
	private SamServiceLoader serviceRef;

	public SamServiceLoader loadServiceDefinition(SamArchitectureLoader loader) {
		try {
			this.loaderRef = loader;
			serviceRef = this.loaderRef.registerService(this);
			loadServiceDefinition();
		} finally {
			this.loaderRef = null;
		}
		return serviceRef;
	}

	abstract public void loadServiceDefinition();

	protected void addInterface(Class<?> interfaceReference) {
		serviceRef.addInterface(interfaceReference);
	}
}
