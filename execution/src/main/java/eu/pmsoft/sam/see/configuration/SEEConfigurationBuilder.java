package eu.pmsoft.sam.see.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;

import java.net.InetSocketAddress;

public class SEEConfigurationBuilder {

    public static SEEConfigurationGrammar configuration() {
        return new SEEConfigurationGrammarObject();
    }

    public static SEENodeConfigurationGrammar nodeConfiguration(){
        return new SEENodeConfigurationGrammarObject();
    }

    public static class SEEConfigurationGrammarObject implements SEEConfigurationGrammar {
        private final ImmutableList.Builder<SamArchitectureDefinition> architectures = ImmutableList.builder();
        private final ImmutableList.Builder<SamServiceImplementationPackageContract> implementationPackages = ImmutableList.builder();


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
            SEEConfiguration config = new SEEConfiguration( architectures.build(), implementationPackages.build());
            return config;
        }

    }

    private static class SEENodeConfigurationGrammarObject implements SEENodeConfigurationGrammar {
        private final ImmutableList.Builder<SEEServiceSetupAction> setupActions = ImmutableList.builder();

        @Override
        public SEENodeConfigurationGrammar setupAction(SEEServiceSetupAction action) {
            setupActions.add(action);
            return this;
        }

        @Override
        public SEENodeConfiguration build() {
            return new SEENodeConfiguration(setupActions.build());
        }

    }
}
