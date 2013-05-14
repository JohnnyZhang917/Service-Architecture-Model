package eu.pmsoft.sam.architecture.model;

import com.google.inject.Module;

public interface SamServiceImplementationDeprecated {

    public SamServiceDeprecated getContract();

    public Module getImplentationModule();

}
