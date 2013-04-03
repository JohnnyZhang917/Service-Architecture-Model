package eu.pmsoft.sam.architecture.loader;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader;
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition;
import eu.pmsoft.sam.architecture.model.SamArchitecture;
import eu.pmsoft.sam.architecture.model.SamCategory;
import eu.pmsoft.sam.architecture.model.SamService;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;
import eu.pmsoft.sam.definition.service.SamServiceLoader;

import java.lang.annotation.Annotation;
import java.util.*;

public class ArchitectureModelLoader implements SamArchitectureLoader {

    public static SamArchitecture loadArchitectureModel(SamArchitectureDefinition definition)
            throws IncorrectArchitectureDefinition {
        ArchitectureModelLoader loader = new ArchitectureModelLoader();
        definition.loadArchitectureDefinition(loader);
        return loader.createModel();
    }

    private SamArchitecture createModel() throws IncorrectArchitectureDefinition {
        ImmutableMap.Builder<String, SamCategory> catBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<ServiceKey, SamService> serviceBuilder = ImmutableMap.builder();
        ImmutableSet.Builder<Annotation> annotationBuilder = ImmutableSet.builder();
        if (categories.isEmpty())
            throw new IncorrectArchitectureDefinition("No categories defined");

        for (SamCategoryLoaderImpl catloader : categories.values()) {
            if (catloader.services.isEmpty())
                throw new IncorrectArchitectureDefinition("Category with no services defined:" + catloader.categoryId);
            ImmutableSet.Builder<SamService> serviceCategoryBuilder = ImmutableSet.builder();
            for (SamCategoryLoaderImpl.SamServiceLoaderImpl serviceLoad : catloader.services) {
                SamServiceObject service = serviceLoad.buildService();
                serviceBuilder.put(service.key, service);
                serviceCategoryBuilder.add(service);
            }
            SamCategoryObject co = new SamCategoryObject(catloader.categoryId, serviceCategoryBuilder.build());
            catBuilder.put(co.categoryId, co);
        }
        ImmutableMap<String, SamCategory> categorySet = catBuilder.build();
        for (SamCategoryLoaderImpl catloader : categories.values()) {
            SamCategory sourceCategoryAccess = categorySet.get(catloader.categoryId);
            for (String accessedCatId : catloader.accessible) {
                if (accessedCatId.compareTo(sourceCategoryAccess.getCategoryId()) == 0) {
                    throw new IncorrectArchitectureDefinition("Self-accessible category: " + accessedCatId);
                }
                SamCategory targetCategoryAccess = categorySet.get(accessedCatId);
                sourceCategoryAccess.getAccessibleCategories().add(targetCategoryAccess);
                targetCategoryAccess.getInverseAccessibleCategories().add(sourceCategoryAccess);
            }
        }
        for (SamCategory categoryReady : categorySet.values()) {
            SamCategoryObject real = (SamCategoryObject) categoryReady;
            real.closeCategory();
        }
        SamArchitectureImpl architectue = new SamArchitectureImpl(categorySet, serviceBuilder.build(), annotationBuilder.build());
        return architectue;
    }

    private final Map<String, SamCategoryLoaderImpl> categories = new HashMap<String, SamCategoryLoaderImpl>();

    @Override
    public SamCategoryLoader createCategory(String categoryName) {
        Preconditions.checkNotNull(categoryName);
        Preconditions.checkState(!categories.containsKey(categoryName));
        SamCategoryLoaderImpl category = new SamCategoryLoaderImpl(categoryName);
        categories.put(categoryName, category);
        return category;
    }

    private static class SamCategoryLoaderImpl implements SamCategoryLoader {

        private final String categoryId;
        private final Set<String> accessible = new HashSet<String>();
        private final Set<SamServiceLoaderImpl> services = new HashSet<SamServiceLoaderImpl>();

        public SamCategoryLoaderImpl(String categoryId) {
            this.categoryId = categoryId;
        }

        @Override
        public SamCategoryLoader accessToCategory(SamCategoryLoader accesibleCategory) {
            SamCategoryLoaderImpl real = (SamCategoryLoaderImpl) accesibleCategory;
            Preconditions.checkState(!accessible.contains(real.categoryId));
            accessible.add(real.categoryId);
            return this;
        }

        @Override
        public SamCategoryLoader withService(SamServiceDefinition serviceDefinition) {
            Preconditions.checkState(!services.contains(serviceDefinition));
            SamServiceLoaderImpl loader = new SamServiceLoaderImpl();
            serviceDefinition.loadServiceDefinition(loader);
            services.add(loader);
            return this;
        }

        private class SamServiceLoaderImpl implements SamServiceLoader {

            private final Set<Key<?>> serviceInterfaces = new HashSet<Key<?>>();
            private Class<? extends SamServiceDefinition> definitionClass = null;

            @Override
            public void addInterface(Class<?> interfaceReference) {
                Preconditions.checkNotNull(interfaceReference);
                Key<?> key = Key.get(interfaceReference);
                Preconditions.checkState(!serviceInterfaces.contains(key));
                serviceInterfaces.add(key);
            }

            @Override
            public void addInterface(Class<?> interfaceReference, Annotation annotation) {
                Preconditions.checkNotNull(interfaceReference);
                Key<?> key = Key.get(interfaceReference, annotation);
                Preconditions.checkState(!serviceInterfaces.contains(key));
                serviceInterfaces.add(key);
            }

            @Override
            public void setupLoadContext(Class<? extends SamServiceDefinition> definitionClass) {
                Preconditions.checkNotNull(definitionClass);
                Preconditions.checkState(this.definitionClass == null);
                this.definitionClass = definitionClass;
            }

            public SamServiceObject buildService() {
                Preconditions.checkState(definitionClass != null);
                Preconditions.checkState(serviceInterfaces.size() > 0);
                ServiceKey key = new ServiceKey(definitionClass);
                return new SamServiceObject(key, ImmutableSet.copyOf(serviceInterfaces));
            }

        }
    }

    private static class SamArchitectureImpl implements SamArchitecture {

        private final ImmutableMap<String, SamCategory> categoriesMap;
        private final ImmutableMap<ServiceKey, SamService> servicesMap;
        private final ImmutableSet<Annotation> architectureAnnotations;

        SamArchitectureImpl(ImmutableMap<String, SamCategory> categoriesMap, ImmutableMap<ServiceKey, SamService> servicesMap,
                            ImmutableSet<Annotation> architectureAnnotations) {
            super();
            this.categoriesMap = categoriesMap;
            this.servicesMap = servicesMap;
            this.architectureAnnotations = architectureAnnotations;
        }

        @Override
        public Collection<SamService> getAllService() {
            return servicesMap.values();
        }

        @Override
        public Collection<SamCategory> getAllCategories() {
            return categoriesMap.values();
        }

        @Override
        public Collection<Annotation> getArchitectureAnnotations() {
            return architectureAnnotations;
        }

        @Override
        public SamCategory getCategory(String categoryId) {
            return categoriesMap.get(categoryId);
        }

        @Override
        public SamService getService(ServiceKey serviceKey) {
            return servicesMap.get(serviceKey);
        }

    }

    private static class SamCategoryObject implements SamCategory {

        private final String categoryId;
        private Set<SamCategory> accesible = Sets.newHashSet();
        private Set<SamCategory> accesibleInverse = Sets.newHashSet();
        private final ImmutableSet<SamService> services;

        public SamCategoryObject(String categoryId, ImmutableSet<SamService> services) {
            super();
            this.categoryId = categoryId;
            this.services = services;
        }

        @Override
        public String getCategoryId() {
            return categoryId;
        }

        @Override
        public Set<SamCategory> getAccessibleCategories() {
            return accesible;
        }

        @Override
        public Set<SamCategory> getInverseAccessibleCategories() {
            return accesibleInverse;
        }

        @Override
        public Set<SamService> getDefinedServices() {
            return services;
        }

        void closeCategory() {
            this.accesible = ImmutableSet.copyOf(accesible);
            this.accesibleInverse = ImmutableSet.copyOf(accesibleInverse);
        }

    }

    private static class SamServiceObject implements SamService {

        private final ServiceKey key;
        private final ImmutableSet<Key<?>> interfaces;

        public SamServiceObject(ServiceKey key, ImmutableSet<Key<?>> interfaces) {
            this.key = key;
            this.interfaces = interfaces;
        }

        @Override
        public ServiceKey getServiceKey() {
            return key;
        }

        @Override
        public Set<Key<?>> getServiceContractAPI() {
            return interfaces;
        }

    }

}
