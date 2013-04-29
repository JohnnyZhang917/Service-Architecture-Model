package eu.pmsoft.sam.see;

import com.google.inject.Inject;
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition;
import eu.pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import eu.pmsoft.sam.architecture.model.SamArchitecture;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.see.api.infrastructure.*;
import eu.pmsoft.sam.see.configuration.SEEConfiguration;

public class SamExecutionEnvironmentInfrastructureSimple implements SamExecutionEnvironmentInfrastructure {
    private final SamArchitectureManagement architectureManager;
    private final SamServiceDiscovery serviceDiscovery;
    private final SamServiceRegistry serviceRegistry;

    @Inject
    public SamExecutionEnvironmentInfrastructureSimple(SamArchitectureManagement architectureManager, SamServiceDiscovery serviceDiscovery, SamServiceRegistry serviceRegistry) {
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
    public SamServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    @Override
    public void setupInfrastructureConfiguration(SEEConfiguration configuration) {
        for (SamArchitectureDefinition architectureDef : configuration.architectures) {
            SamArchitecture architecture;
            try {
                architecture = ArchitectureModelLoader.loadArchitectureModel(architectureDef);
                architectureManager.registerArchitecture(architecture);
            } catch (IncorrectArchitectureDefinition e) {
                throw new RuntimeException(e);
            }
        }
        for (SamServiceImplementationPackageContract implPackage : configuration.implementationPackages) {
            serviceRegistry.registerServiceImplementationPackage(implPackage);
        }
    }
}
