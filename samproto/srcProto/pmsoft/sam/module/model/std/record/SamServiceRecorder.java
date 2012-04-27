package pmsoft.sam.module.model.std.record;

import pmsoft.sam.meta.RelationRegistry;
import pmsoft.sam.module.definition.architecture.grammar.SamServiceLoader;

import com.google.inject.Inject;

public class SamServiceRecorder implements SamServiceLoader {
	@Inject
	private RelationRegistry<SamServiceLoader, Class<?>> serviceInterfaces;

	public void addInterface(Class<?> interfaceReference) {
		serviceInterfaces.addRelationEntry(this, interfaceReference);
	}

}
