package pmsoft.sam.module.model;

import java.util.Collection;
import java.util.Set;

//FIXME this version of architecture don't define binding annotations
// wait until it is really necessary just to know the practical reasons 
public interface SamArchitecture {

	Collection<SamService> getAllService();
	Collection<SamCategory> getAllCategories();
	SamCategory getCategory(String categoryId);
	SamService getService(ServiceKey serviceKey);
	
	Set<Class<?>> getServiceInterfaces(ServiceKey serviceKey);
	Set<SamCategory> getAccessibleCategories(String categoryId);
	Set<SamCategory> getInverseAccessibleCategories(String categoryId);
	Set<SamService> getServiceInCategory(String categoryId);

}
