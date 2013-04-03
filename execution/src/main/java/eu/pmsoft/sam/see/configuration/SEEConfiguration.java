package eu.pmsoft.sam.see.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.see.SEEServiceSetupAction;

import java.net.InetSocketAddress;

public class SEEConfiguration {
    public final ImmutableList<Module> pluginModules;
    public final ImmutableList<SamArchitectureDefinition> architectures;
    public final ImmutableList<SamServiceImplementationPackageContract> implementationPackages;
    public final ImmutableList<SEEServiceSetupAction> setupActions;
    public final InetSocketAddress address;

    SEEConfiguration(ImmutableList<Module> pluginModules, ImmutableList<SamArchitectureDefinition> architectures,
                     ImmutableList<SamServiceImplementationPackageContract> implementationPackages, ImmutableList<SEEServiceSetupAction> setupActions, InetSocketAddress address) {
        this.pluginModules = pluginModules;
        this.architectures = architectures;
        this.implementationPackages = implementationPackages;
        this.setupActions = setupActions;
        this.address = address;
    }

}
