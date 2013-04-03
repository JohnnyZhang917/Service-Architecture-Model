package eu.pmsoft.sam.protocol.transport.data;


public class PendingDataInstanceReference extends AbstractInstanceReference {

    /**
     *
     */
    private static final long serialVersionUID = 3892653795826758252L;
    private final Class<?> dataType;

    public PendingDataInstanceReference(int instanceNr, Class<?> dataType) {
        super(instanceNr);
        this.dataType = dataType;
    }

    @Override
    public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
        api.visitPendingDataInstance(this);
    }

    @Override
    public String toString() {
        return "#" + instanceNr + "->PendingDataInstanceReference [dataType=" + dataType + ", instanceNr=" + instanceNr + "]";
    }

    public Class<?> getDataType() {
        return dataType;
    }
}
