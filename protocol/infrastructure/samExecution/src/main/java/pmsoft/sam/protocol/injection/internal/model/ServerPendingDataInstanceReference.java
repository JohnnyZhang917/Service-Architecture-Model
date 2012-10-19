package pmsoft.sam.protocol.injection.internal.model;


public class ServerPendingDataInstanceReference extends PendingDataInstanceReference {

	public ServerPendingDataInstanceReference(int instanceNr, Class<?> dataType) {
		super(instanceNr, dataType);
	}

	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitServerPendingDataInstance(this);
	}

	@Override
	public String toString() {
		return "#"+instanceNr+"->ServerPendingDataInstanceReference [instanceNr=" + instanceNr + "]";
	}
	
	
}
