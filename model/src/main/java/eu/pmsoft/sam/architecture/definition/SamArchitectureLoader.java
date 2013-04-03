package eu.pmsoft.sam.architecture.definition;

import eu.pmsoft.sam.definition.service.SamServiceDefinition;


public interface SamArchitectureLoader {

    SamCategoryLoader createCategory(String categoryName);

    static public interface SamCategoryLoader {

        public SamCategoryLoader accessToCategory(SamCategoryLoader accesibleCategory);

        public SamCategoryLoader withService(SamServiceDefinition serviceDefinition);

    }

}
