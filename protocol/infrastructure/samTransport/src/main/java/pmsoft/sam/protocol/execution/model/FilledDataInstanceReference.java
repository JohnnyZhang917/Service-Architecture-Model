package pmsoft.sam.protocol.execution.model;


public class FilledDataInstanceReference extends DataObjectInstanceReference{

	public FilledDataInstanceReference(int instanceNr, Object objectReference) {
		super(instanceNr, objectReference);
	}
	
	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitFilledDataInstance(this);
	}

	@Override
	public String toString() {
		return "#"+instanceNr+"->FilledDataInstanceReference [instanceNr=" + instanceNr + ", getObjectReference()=" + getObjectReference() + "]";
	}


}