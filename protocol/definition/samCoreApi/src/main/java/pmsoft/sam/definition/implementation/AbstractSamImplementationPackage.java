package pmsoft.sam.definition.implementation;

import pmsoft.sam.definition.service.SamServiceDefinition;

public abstract class AbstractSamImplementationPackage implements
        SamServiceImplementationPackageContract {

    private SamServicePackageLoader reader;

    @Override
    public void loadContractPackage(SamServicePackageLoader reader) {
        try {
            this.reader = reader;
            packageDefinition();
        } finally {
            this.reader = null;
        }
    }

    public abstract void packageDefinition();

    protected final void registerImplementation(
            AbstractSamServiceImplementationDefinition<? extends SamServiceDefinition> serviceImplementationContract) {
        reader.registerImplementation(serviceImplementationContract);
    }

}
