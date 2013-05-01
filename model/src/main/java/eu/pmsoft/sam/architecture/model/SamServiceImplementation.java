package eu.pmsoft.sam.architecture.model;

import com.google.inject.Module;

@Deprecated
public interface SamServiceImplementation {

    public SamService getContract();

    public Module getImplentationModule();

}
