package pmsoft.sam.module.model.std;

import pmsoft.sam.meta.RecorderRelationRegistry;
import pmsoft.sam.meta.RecorderTypeRegistry;
import pmsoft.sam.meta.RelationRegistry;
import pmsoft.sam.meta.TypeRegistry;
import pmsoft.sam.module.definition.architecture.SamArchitectureDefinition;
import pmsoft.sam.module.definition.architecture.grammar.SamArchitectureLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamArchitectureParser;
import pmsoft.sam.module.definition.architecture.grammar.SamCategoryLoader;
import pmsoft.sam.module.definition.architecture.grammar.SamServiceLoader;
import pmsoft.sam.module.model.SamArchitecture;
import pmsoft.sam.module.model.ServiceKey;
import pmsoft.sam.module.model.std.record.SamArchitectureFinalParser;
import pmsoft.sam.module.model.std.record.SamArchitectureRecorder;
import pmsoft.sam.module.model.std.record.SamCategoryRecorder;
import pmsoft.sam.module.model.std.record.SamServiceRecorder;

import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class SamArchitectureLoaderStdModule extends PrivateModule {

	@Override
	protected void configure() {
		// Parser
		bind(SamArchitectureParser.class).to(SamArchitectureFinalParser.class);
		bind(SamArchitectureLoader.class).to(SamArchitectureRecorder.class);
		
		// Classes
		bind(SamCategoryLoader.class).to(SamCategoryRecorder.class);
		bind(SamServiceLoader.class).to(SamServiceRecorder.class);

		bind(new TypeLiteral<RelationRegistry<SamCategoryLoader, SamServiceLoader>>() {
		}).toInstance(new RecorderRelationRegistry<SamCategoryLoader, SamServiceLoader>());
		bind(new TypeLiteral<RelationRegistry<SamCategoryLoader, SamCategoryLoader>>() {
		}).toInstance(new RecorderRelationRegistry<SamCategoryLoader, SamCategoryLoader>());
		bind(new TypeLiteral<RelationRegistry<SamServiceLoader, Class<?>>>() {
		}).toInstance(new RecorderRelationRegistry<SamServiceLoader, Class<?>>());

		expose(SamArchitecture.class);
	}
	
	@Provides @Singleton
	public TypeRegistry<String, SamCategoryLoader> typeCategory(Provider<SamCategoryLoader> provider){
		return new RecorderTypeRegistry<String, SamCategoryLoader>(provider);
	}
	
	@Provides @Singleton
	public TypeRegistry<ServiceKey, SamServiceLoader> typeService(Provider<SamServiceLoader> provider){
		return new RecorderTypeRegistry<ServiceKey, SamServiceLoader>(provider);
	}

	@Provides @Singleton
	public SamArchitecture provideSamArchitecture(SamArchitectureDefinition definition, SamArchitectureParser parser) {
		return parser.createArchitectureModel(definition);
	}

}
