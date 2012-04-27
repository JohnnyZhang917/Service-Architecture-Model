package pmsoft.sam.module.model.std.record;

import com.google.inject.Inject;

import pmsoft.sam.meta.RelationRegistry;
import pmsoft.sam.module.definition.architecture.grammar.SamCategoryLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamServiceLoader;

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

}
