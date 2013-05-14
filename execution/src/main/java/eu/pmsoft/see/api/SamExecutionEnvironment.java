package eu.pmsoft.see.api;

import eu.pmsoft.see.api.infrastructure.SamExecutionEnvironmentInfrastructure;
import eu.pmsoft.see.api.setup.SamExecutionNode;

public interface SamExecutionEnvironment {

    public SamExecutionEnvironmentInfrastructure getInfrastructureApi();

    public SamExecutionNode createExecutionNode(int port);
}
