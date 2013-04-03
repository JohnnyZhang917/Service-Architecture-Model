package pmsoft.sam.architecture.model;

import com.google.inject.Module;

public interface SamServiceImplementation {

    public SamService getContract();

    public Module getImplentationModule();

}
