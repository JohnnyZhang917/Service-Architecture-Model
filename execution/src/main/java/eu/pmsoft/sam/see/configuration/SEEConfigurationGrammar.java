package eu.pmsoft.sam.see.configuration;

import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;

public interface SEEConfigurationGrammar {

    public SEEConfigurationGrammar architecture(SamArchitectureDefinition architecture);

    public SEEConfigurationGrammar implementationPackage(SamServiceImplementationPackageContract implementationPackage);

    public SEEConfiguration build();
}
