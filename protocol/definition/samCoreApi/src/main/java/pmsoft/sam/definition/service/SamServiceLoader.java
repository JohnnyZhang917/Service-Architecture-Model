package pmsoft.sam.definition.service;

public interface SamServiceLoader {

	void addInterface(Class<?> interfaceReference);

	void setupLoadContext(Class<? extends SamServiceDefinition> definitionClass);

}
