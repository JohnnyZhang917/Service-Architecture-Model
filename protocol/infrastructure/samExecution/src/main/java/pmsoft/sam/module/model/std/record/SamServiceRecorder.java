package pmsoft.sam.module.model.std.record;

import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.definition.service.SamServiceLoader;
import pmsoft.sam.meta.RelationRegistry;

import com.google.inject.Inject;

public class SamServiceRecorder implements SamServiceLoader {
	@Inject
	private RelationRegistry<SamServiceLoader, Class<?>> serviceInterfaces;

	public void addInterface(Class<?> interfaceReference) {
		serviceInterfaces.addRelationEntry(this, interfaceReference);
	}

	@Override
	public void setupLoadContext(Class<? extends SamServiceDefinition> definitionClass) {
		// TODO Auto-generated method stub
		
	}

}
