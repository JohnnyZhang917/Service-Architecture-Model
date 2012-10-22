package pmsoft.sam.see;

import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;

public class SEEConfiguration {

	final int port;
	final ImmutableList<Module> pluginModules;
	final ImmutableList<SamArchitectureDefinition> architectures;
	final ImmutableList<SamServiceImplementationPackageContract> implementationPackages;
	final ImmutableList<SEEServiceSetupAction> setupActions;

	public SEEConfiguration(int port, ImmutableList<Module> pluginModules, ImmutableList<SamArchitectureDefinition> architectures,
			ImmutableList<SamServiceImplementationPackageContract> implementationPackages, ImmutableList<SEEServiceSetupAction> setupActions) {
		super();
		this.port = port;
		this.pluginModules = pluginModules;
		this.architectures = architectures;
		this.implementationPackages = implementationPackages;
		this.setupActions = setupActions;
	}

}
