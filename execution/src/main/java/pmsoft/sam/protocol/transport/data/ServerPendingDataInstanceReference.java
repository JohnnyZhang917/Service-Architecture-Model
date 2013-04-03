package pmsoft.sam.protocol.transport.data;


public class ServerPendingDataInstanceReference extends PendingDataInstanceReference {

    /**
     *
     */
    private static final long serialVersionUID = 5011356014147884983L;

    public ServerPendingDataInstanceReference(int instanceNr, Class<?> dataType) {
        super(instanceNr, dataType);
    }

    @Override
    public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
        api.visitServerPendingDataInstance(this);
    }

    @Override
    public String toString() {
        return "#" + instanceNr + "->ServerPendingDataInstanceReference [instanceNr=" + instanceNr + "]";
    }


}
