package eu.pmsoft.sam.see.api;

import eu.pmsoft.sam.see.api.infrastructure.SamExecutionEnvironmentInfrastructure;
import eu.pmsoft.sam.see.api.setup.SamExecutionNode;

public interface SamExecutionEnvironment {

    public SamExecutionEnvironmentInfrastructure getInfrastructureApi();

    public SamExecutionNode createExecutionNode(int port);
}
