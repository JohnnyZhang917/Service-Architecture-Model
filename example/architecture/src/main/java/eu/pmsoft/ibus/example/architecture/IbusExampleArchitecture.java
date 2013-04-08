package eu.pmsoft.ibus.example.architecture;

import eu.pmsoft.sam.architecture.definition.AbstractSamArchitectureDefinition;
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader;

public class IbusExampleArchitecture extends AbstractSamArchitectureDefinition {

    public String CITIZEN_ACCESS_CATEGORY = "CITIZEN_ACCESS";

    public String INFORMATION_MODEL_CATEGORY = "INFORMATION_MODEL";
    public String PUBLIC_SERVICE_CATEGORY = "PUBLIC_SERVICE";

    public String PRODUCTION_SERVICE_CATEGORY = "PRODUCTION_SERVICE";
    public String TRADE_SERVICE_CATEGORY = "TRADE_SERVICE";

    public String ECONOMY_SERVICE_CATEGORY = "ECONOMY_SERVICE";

    public String COMMON_TRANSACTION_CATEGORY = "COMMON_TRANSACTION";

    @Override
    protected void loadArchitectureDefinition() {
        SamArchitectureLoader.SamCategoryLoader citizen = createCategory(CITIZEN_ACCESS_CATEGORY);

        SamArchitectureLoader.SamCategoryLoader publicService = createCategory(PUBLIC_SERVICE_CATEGORY);
        SamArchitectureLoader.SamCategoryLoader information = createCategory(INFORMATION_MODEL_CATEGORY);

        SamArchitectureLoader.SamCategoryLoader trade = createCategory(TRADE_SERVICE_CATEGORY);
        SamArchitectureLoader.SamCategoryLoader economy = createCategory(ECONOMY_SERVICE_CATEGORY);

        SamArchitectureLoader.SamCategoryLoader production = createCategory(PRODUCTION_SERVICE_CATEGORY);

        SamArchitectureLoader.SamCategoryLoader transaction = createCategory(COMMON_TRANSACTION_CATEGORY);

        citizen .accessToCategory(information)
                .accessToCategory(publicService);

        publicService
                .accessToCategory(information)
                .accessToCategory(trade)
                .accessToCategory(transaction)
                .accessToCategory(economy);

        information
                .accessToCategory(transaction);

        trade
                .accessToCategory(economy)
                .accessToCategory(production)
                .accessToCategory(transaction);

        economy
                .accessToCategory(production)
                .accessToCategory(transaction);

        production
                .accessToCategory(transaction);




    }
}
