package pmsoft.sam.module.model.std.record;

import pmsoft.sam.definition.architecture.SamCategoryLoader;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.definition.service.SamServiceLoader;
import pmsoft.sam.meta.RelationRegistry;

import com.google.inject.Inject;

public class SamCategoryRecorder implements SamCategoryLoader {

	@Inject
	private RelationRegistry<SamCategoryLoader,SamCategoryLoader> categoryAccesibility;
	
	@Inject
	private RelationRegistry<SamCategoryLoader,SamServiceLoader> containsServices;
	
	
	public void addService(SamServiceLoader service) {
		containsServices.addRelationEntry(this, service);	
	}

	public void addAccessToCategory(SamCategoryLoader accesibleCategory) {
		categoryAccesibility.addRelationEntry(this, accesibleCategory);
	}

	@Override
	public void addInterface(Class<?> interfaceReference) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupLoadContext(Class<? extends SamServiceDefinition> definitionClass) {
		// TODO Auto-generated method stub
		
	}

}
