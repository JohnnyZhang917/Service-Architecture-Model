package eu.pmsoft.see.api.data.architecture;

import eu.pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;
import eu.pmsoft.see.api.data.architecture.service.*;

public class SeeTestArchitecture extends AbstractSamArchitectureDefinition {

    public static String signature = "Test Architecture 0.1";

    public SeeTestArchitecture() {
        super(signature);
    }

    @Override
    protected void loadArchitectureDefinition() {
        SamCategoryLoader test = createCategory("Test");
        test.withService(new TestServiceOne());
        test.withService(new TestServiceTwo());
        test.withService(new TestServiceZero());

        SamCategoryLoader shopping = createCategory("Shopping");
        shopping.withService(new ShoppingService());
        shopping.withService(new CourierService());
        shopping.withService(new StoreService());

    }

}
