package pmsoft.sam.module.model.std.data;

import java.util.Collection;
import java.util.Set;

import pmsoft.sam.module.model.SamArchitecture;
import pmsoft.sam.module.model.SamCategory;
import pmsoft.sam.module.model.SamService;
import pmsoft.sam.module.model.ServiceKey;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSetMultimap;

public class SamArchitectureModel implements SamArchitecture {

	//TYPES
	private ImmutableBiMap<String, SamCategory> typeCategory;
	private ImmutableBiMap<ServiceKey, SamService> typeService;

	// SERVICE relations
	private ImmutableSetMultimap<ServiceKey, Class<?>> serviceInterfaces;

	// CATEGORY relations
	private ImmutableSetMultimap<String, SamCategory> categoryAccesibility;
	private ImmutableSetMultimap<String, SamCategory> categoryAccesibilityInverse;
	private ImmutableSetMultimap<String, SamService> serviceInCategory;


	public void setTypeCategory(ImmutableBiMap<String, SamCategory> typeCategory) {
		this.typeCategory = typeCategory;
	}

	public void setTypeService(ImmutableBiMap<ServiceKey, SamService> typeService) {
		this.typeService = typeService;
	}

	public void setServiceInterfaces(ImmutableSetMultimap<ServiceKey, Class<?>> serviceInterfaces) {
		this.serviceInterfaces = serviceInterfaces;
	}

	public void setCategoryAccesibility(ImmutableSetMultimap<String, SamCategory> categoryAccesibility) {
		this.categoryAccesibility = categoryAccesibility;
	}

	public void setCategoryAccesibilityInverse(ImmutableSetMultimap<String, SamCategory> categoryAccesibilityInverse) {
		this.categoryAccesibilityInverse = categoryAccesibilityInverse;
	}

	public void setServiceInCategory(ImmutableSetMultimap<String, SamService> serviceInCategory) {
		this.serviceInCategory = serviceInCategory;
	}
	
	
	/**
	 * API interface
	 */
	

	public Collection<SamService> getAllService() {
		return typeService.values();
	}

	public Collection<SamCategory> getAllCategories() {
		return typeCategory.values();
	}

	public SamCategory getCategory(String categoryId) {
		return typeCategory.get(categoryId);
	}

	public SamService getService(ServiceKey serviceKey) {
		return typeService.get(serviceKey);
	}

	public Set<Class<?>> getServiceInterfaces(ServiceKey serviceKey) {
		return serviceInterfaces.get(serviceKey);
	}

	public Set<SamCategory> getAccessibleCategories(String categoryId) {
		return categoryAccesibility.get(categoryId);
	}

	public Set<SamCategory> getInverseAccessibleCategories(String categoryId) {
		return categoryAccesibilityInverse.get(categoryId);
	}

	public Set<SamService> getServiceInCategory(String categoryId) {
		return serviceInCategory.get(categoryId);
	}

}
