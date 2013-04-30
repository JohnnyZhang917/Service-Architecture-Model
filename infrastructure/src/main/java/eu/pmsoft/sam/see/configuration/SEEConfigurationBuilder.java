package eu.pmsoft.sam.see.configuration;

import com.google.common.collect.ImmutableList;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.model.SEEConfiguration;
import eu.pmsoft.sam.model.SEENodeConfiguration;
import scala.collection.JavaConversions;

import java.util.HashSet;
import java.util.Set;

public class SEEConfigurationBuilder {

    public static SEEConfigurationGrammar configuration() {
        return new SEEConfigurationGrammarObject();
    }


    public static class SEEConfigurationGrammarObject implements SEEConfigurationGrammar {
        private final Set<SamArchitectureDefinition> architectures = new HashSet<SamArchitectureDefinition>();
        private final Set<SamServiceImplementationPackageContract> implementationPackages = new HashSet<SamServiceImplementationPackageContract>();


        @Override
        public SEEConfigurationGrammar architecture(SamArchitectureDefinition architecture) {
            architectures.add(architecture);
            return this;
        }

        @Override
        public SEEConfigurationGrammar implementationPackage(SamServiceImplementationPackageContract implementationPackage) {
            implementationPackages.add(implementationPackage);
            return this;
        }


        @Override
        public SEEConfiguration build() {
//            SEEConfigurationDeprecated config = new SEEConfigurationDeprecated( architectures.build(), implementationPackages.build());


            return null;
        }

    }


}
