package eu.pmsoft.sam.see.api.infrastructure;

import eu.pmsoft.sam.see.configuration.SEEConfiguration;

public interface SamExecutionEnvironmentInfrastructure {

    public SamArchitectureManagement getArchitectureManager();

    public SamArchitectureRegistry getArchitectureRegistry();

    public SamServiceDiscovery getServiceDiscovery();

    public SamServiceRegistry getServiceRegistry();

    public void setupInfrastructureConfiguration(SEEConfiguration configuration);
}
