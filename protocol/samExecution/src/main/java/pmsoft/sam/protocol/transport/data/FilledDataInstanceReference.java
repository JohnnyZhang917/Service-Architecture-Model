package pmsoft.sam.protocol.transport.data;


public class FilledDataInstanceReference extends ClientDataObjectInstanceReference {

    /**
     *
     */
    private static final long serialVersionUID = 6303415163708224368L;

    public FilledDataInstanceReference(int instanceNr, Object objectReference) {
        super(instanceNr, objectReference);
    }

    @Override
    public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
        api.visitFilledDataInstance(this);
    }

    @Override
    public String toString() {
        return "#" + instanceNr + "->FilledDataInstanceReference [instanceNr=" + instanceNr + ", getObjectReference()=" + getObjectReference() + "]";
    }


}
