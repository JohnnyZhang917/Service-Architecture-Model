package eu.pmsoft.sam.see.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.see.SEEServiceSetupAction;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;

public class SEEConfiguration {
    public final ImmutableList<Module> pluginModules;
    public final ImmutableList<SamArchitectureDefinition> architectures;
    public final ImmutableList<SamServiceImplementationPackageContract> implementationPackages;
    public final ImmutableList<SEEServiceSetupAction> setupActions;

    @Nullable
    public final InetSocketAddress address;

    SEEConfiguration(ImmutableList<Module> pluginModules, ImmutableList<SamArchitectureDefinition> architectures,
                     ImmutableList<SamServiceImplementationPackageContract> implementationPackages, ImmutableList<SEEServiceSetupAction> setupActions, @Nullable InetSocketAddress address) {
        this.pluginModules = pluginModules;
        this.architectures = architectures;
        this.implementationPackages = implementationPackages;
        this.setupActions = setupActions;
        this.address = address;
    }

    public static SEEConfiguration empty() {
        return new SEEConfiguration(ImmutableList.<Module>of(),
                ImmutableList.<SamArchitectureDefinition>of(),
                ImmutableList.<SamServiceImplementationPackageContract>of(),
                ImmutableList.<SEEServiceSetupAction>of(),
                null);
    }


    public static SEEConfiguration emptyOnPort(int port) {
        return new SEEConfiguration(ImmutableList.<Module>of(),
                ImmutableList.<SamArchitectureDefinition>of(),
                ImmutableList.<SamServiceImplementationPackageContract>of(),
                ImmutableList.<SEEServiceSetupAction>of(),
                new InetSocketAddress(port));
    }

}
