package pmsoft.sam.see;

import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;

import java.net.InetSocketAddress;

public class SEEConfiguration {

    final ImmutableList<Module> pluginModules;
	final ImmutableList<SamArchitectureDefinition> architectures;
	final ImmutableList<SamServiceImplementationPackageContract> implementationPackages;
	final ImmutableList<SEEServiceSetupAction> setupActions;
    final InetSocketAddress address;

    SEEConfiguration(ImmutableList<Module> pluginModules, ImmutableList<SamArchitectureDefinition> architectures,
                     ImmutableList<SamServiceImplementationPackageContract> implementationPackages, ImmutableList<SEEServiceSetupAction> setupActions, InetSocketAddress address) {
        this.pluginModules = pluginModules;
		this.architectures = architectures;
		this.implementationPackages = implementationPackages;
		this.setupActions = setupActions;
        this.address = address;
    }

}
