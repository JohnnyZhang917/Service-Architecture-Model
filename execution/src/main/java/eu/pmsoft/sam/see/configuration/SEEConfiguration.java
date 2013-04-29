package eu.pmsoft.sam.see.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;

public class SEEConfiguration {
    public final ImmutableList<SamArchitectureDefinition> architectures;
    public final ImmutableList<SamServiceImplementationPackageContract> implementationPackages;


    SEEConfiguration(ImmutableList<SamArchitectureDefinition> architectures,
                     ImmutableList<SamServiceImplementationPackageContract> implementationPackages) {
        this.architectures = architectures;
        this.implementationPackages = implementationPackages;
    }

    public static SEEConfiguration empty() {
        return new SEEConfiguration(ImmutableList.<SamArchitectureDefinition>of(),ImmutableList.<SamServiceImplementationPackageContract>of());
    }

}
