package eu.pmsoft.see.api.infrastructure;

public interface SamExecutionEnvironmentInfrastructure {

    public SamArchitectureManagement getArchitectureManager();

    public SamArchitectureRegistry getArchitectureRegistry();

    public SamServiceDiscovery getServiceDiscovery();

    public SamServiceRegistryDeprecated getServiceRegistry();

//    public void setupInfrastructureConfiguration(SEEConfiguration configuration);
}
