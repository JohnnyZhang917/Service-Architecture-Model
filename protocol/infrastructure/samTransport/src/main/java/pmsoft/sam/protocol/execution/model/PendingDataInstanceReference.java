package pmsoft.sam.protocol.execution.model;


public class PendingDataInstanceReference extends AbstractInstanceReference {

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
		return "#"+instanceNr+"->PendingDataInstanceReference [dataType=" + dataType + ", instanceNr=" + instanceNr + "]";
	}

	public Class<?> getDataType() {
		return dataType;
	}
	

}