package pmsoft.sam.module.model.std.record;

import pmsoft.sam.definition.architecture.SamArchitectureDefinition;
import pmsoft.sam.definition.architecture.SamArchitectureLoader;
import pmsoft.sam.definition.architecture.SamCategoryLoader;
import pmsoft.sam.definition.service.SamServiceLoader;
import pmsoft.sam.meta.RelationRegistry;
import pmsoft.sam.meta.SamArchitectureParser;
import pmsoft.sam.meta.TypeRegistry;
import pmsoft.sam.model.architecture.SamArchitecture;
import pmsoft.sam.model.architecture.SamCategory;
import pmsoft.sam.model.architecture.SamService;
import pmsoft.sam.model.architecture.ServiceKey;
import pmsoft.sam.module.model.std.data.SamArchitectureModel;
import pmsoft.sam.module.model.std.data.SamCategoryModel;
import pmsoft.sam.module.model.std.data.SamServiceModel;
import ch.lambdaj.function.convert.Converter;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.inject.Inject;

public class SamArchitectureFinalParser implements SamArchitectureParser {

	private final TypeRegistry<String, SamCategoryLoader> categorySet;
	private final TypeRegistry<ServiceKey, SamServiceLoader> serviceSet;
	private final RelationRegistry<SamCategoryLoader, SamCategoryLoader> categoryAccesibility;
	private final RelationRegistry<SamCategoryLoader, SamServiceLoader> containsServices;
	private RelationRegistry<SamServiceLoader, Class<?>> serviceInterfaces;

	private final SamArchitectureLoader loader;

	@Inject
	public SamArchitectureFinalParser(TypeRegistry<String, SamCategoryLoader> categorySet,
			TypeRegistry<ServiceKey, SamServiceLoader> serviceSet,
			RelationRegistry<SamCategoryLoader, SamCategoryLoader> categoryAccesibility,
			RelationRegistry<SamCategoryLoader, SamServiceLoader> containsServices,
			RelationRegistry<SamServiceLoader, Class<?>> serviceInterfaces, SamArchitectureLoader loader) {
		super();
		this.categorySet = categorySet;
		this.serviceSet = serviceSet;
		this.categoryAccesibility = categoryAccesibility;
		this.containsServices = containsServices;
		this.serviceInterfaces = serviceInterfaces;
		this.loader = loader;
	}

	public SamArchitecture createArchitectureModel(SamArchitectureDefinition definition) {
		definition.loadArchitectureDefinition(loader);

		final SamArchitectureModel model = new SamArchitectureModel();
		Converter<SamCategoryLoader, String> categoryKeyConverter = new Converter<SamCategoryLoader, String>() {
			public String convert(SamCategoryLoader arg0) {
				return categorySet.getInstanceKey(arg0);
			}
		};
		Converter<SamServiceLoader, ServiceKey> serviceKeyConverter = new Converter<SamServiceLoader, ServiceKey>() {
			public ServiceKey convert(SamServiceLoader arg0) {
				return serviceSet.getInstanceKey(arg0);
			}
		};
		final ImmutableBiMap<String, SamCategory> typeCategory = categorySet
				.createImmutableView(new Converter<SamCategoryLoader, SamCategory>() {
					public SamCategory convert(SamCategoryLoader arg0) {
						return new SamCategoryModel(categorySet.getInstanceKey(arg0), model);
					}
				});
		final ImmutableBiMap<ServiceKey, SamService> typeService = serviceSet
				.createImmutableView(new Converter<SamServiceLoader, SamService>() {
					public SamService convert(SamServiceLoader arg0) {
						return new SamServiceModel(serviceSet.getInstanceKey(arg0), model);
					}
				});

		Converter<SamCategoryLoader, SamCategory> categoryLoaderToViewConverter = new Converter<SamCategoryLoader, SamCategory>() {
			public SamCategory convert(SamCategoryLoader arg0) {
				return typeCategory.get(categorySet.getInstanceKey(arg0));
			}
		};

		Converter<SamServiceLoader, SamService> serviceLoaderToViewConverter = new Converter<SamServiceLoader, SamService>() {
			public SamService convert(SamServiceLoader arg0) {
				return typeService.get(serviceSet.getInstanceKey(arg0));
			}
		};

		ImmutableSetMultimap<String, SamCategory> categoryAccesibilityRelation = categoryAccesibility.createImmutableView(
				categoryKeyConverter, categoryLoaderToViewConverter);
		ImmutableSetMultimap<String, SamCategory> categoryAccesibilityRelationInverser = categoryAccesibility.inverse()
				.createImmutableView(categoryKeyConverter, categoryLoaderToViewConverter);
		ImmutableSetMultimap<String, SamService> containsServicesRelation = containsServices.createImmutableView(
				categoryKeyConverter, serviceLoaderToViewConverter);
		ImmutableSetMultimap<ServiceKey, Class<?>> serviceInterfacesRelation = serviceInterfaces
				.createImmutableView(serviceKeyConverter);

		model.setTypeCategory(typeCategory);
		model.setTypeService(typeService);
		model.setServiceInterfaces(serviceInterfacesRelation);
		model.setServiceInCategory(containsServicesRelation);
		model.setCategoryAccesibility(categoryAccesibilityRelation);
		model.setCategoryAccesibilityInverse(categoryAccesibilityRelationInverser);
		return model;
	}
}
