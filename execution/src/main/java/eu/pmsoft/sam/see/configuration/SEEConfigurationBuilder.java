package eu.pmsoft.sam.see.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.see.SEEServiceSetupAction;

import java.net.InetSocketAddress;

public class SEEConfigurationBuilder {

    public static SEEConfigurationGrammar configuration() {
        return new SEEConfigurationGrammarObject();
    }

    public static class SEEConfigurationGrammarObject implements SEEConfigurationGrammar {
        private final ImmutableList.Builder<Module> pluginModules = ImmutableList.builder();
        private final ImmutableList.Builder<SamArchitectureDefinition> architectures = ImmutableList.builder();
        private final ImmutableList.Builder<SamServiceImplementationPackageContract> implementationPackages = ImmutableList.builder();
        private final ImmutableList.Builder<SEEServiceSetupAction> setupActions = ImmutableList.builder();

        @Override
        public SEEConfigurationGrammar withPlugin(Module plugin) {
            pluginModules.add(plugin);
            return this;
        }

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
        public SEEConfigurationGrammar setupAction(SEEServiceSetupAction action) {
            setupActions.add(action);
            return this;
        }

        @Override
        public SEEConfiguration bindToAddress(InetSocketAddress address) {
            SEEConfiguration config = new SEEConfiguration(pluginModules.build(), architectures.build(), implementationPackages.build(),
                    setupActions.build(), address);
            return config;
        }

    }
}
