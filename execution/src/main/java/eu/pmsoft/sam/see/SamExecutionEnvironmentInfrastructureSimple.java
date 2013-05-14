package eu.pmsoft.sam.see;

import com.google.inject.Inject;
import eu.pmsoft.see.api.infrastructure.*;

public class SamExecutionEnvironmentInfrastructureSimple implements SamExecutionEnvironmentInfrastructure {
    private final SamArchitectureManagement architectureManager;
    private final SamServiceDiscovery serviceDiscovery;
    private final SamServiceRegistryDeprecated serviceRegistry;

    @Inject
    public SamExecutionEnvironmentInfrastructureSimple(SamArchitectureManagement architectureManager, SamServiceDiscovery serviceDiscovery, SamServiceRegistryDeprecated serviceRegistry) {
        this.architectureManager = architectureManager;
        this.serviceDiscovery = serviceDiscovery;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public SamArchitectureManagement getArchitectureManager() {
        return architectureManager;
    }

    @Override
    public SamArchitectureRegistry getArchitectureRegistry() {
        return architectureManager;
    }

    @Override
    public SamServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    @Override
    public SamServiceRegistryDeprecated getServiceRegistry() {
        return serviceRegistry;
    }

//    @Override
//    public void setupInfrastructureConfiguration(SEEConfiguration configuration) {
//        for (SamArchitectureDefinition architectureDef : configuration.architectures) {
//            SamArchitecture architecture;
//            try {
//                architecture = ArchitectureModelLoader.loadArchitectureModel(architectureDef);
//                architectureManager.registerArchitecture(architecture);
//            } catch (IncorrectArchitectureDefinition e) {
//                throw new RuntimeException(e);
//            }
//        }
//        for (SamServiceImplementationPackageContract implPackage : configuration.implementationPackages) {
//            serviceRegistry.registerServiceImplementationPackage(implPackage);
//        }
//    }
}
