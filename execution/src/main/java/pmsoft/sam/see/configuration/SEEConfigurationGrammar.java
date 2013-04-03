package pmsoft.sam.see.configuration;

import com.google.inject.Module;
import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import pmsoft.sam.see.SEEServiceSetupAction;

import java.net.InetSocketAddress;

public interface SEEConfigurationGrammar {

    public SEEConfigurationGrammar withPlugin(Module plugin);

    public SEEConfigurationGrammar architecture(SamArchitectureDefinition architecture);

    public SEEConfigurationGrammar implementationPackage(SamServiceImplementationPackageContract implementationPackage);

    public SEEConfigurationGrammar setupAction(SEEServiceSetupAction action);

    public SEEConfiguration bindToAddress(InetSocketAddress address);
}
