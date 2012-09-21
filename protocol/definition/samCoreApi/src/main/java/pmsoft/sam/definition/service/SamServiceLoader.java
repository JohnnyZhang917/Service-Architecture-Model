package pmsoft.sam.definition.service;

public interface SamServiceLoader {
	// regular grammar:
	// Service: interface* definitionClass

	void addInterface(Class<?> interfaceReference);

	void setupLoadContext(Class<? extends SamServiceDefinition> definitionClass);

}
