package pmsoft.sam.protocol.execution.model;


public interface InstanceMergeVisitor {

	public <T> void visitBindingKey(BindingKeyInstanceReference<T> bindingKeyInstanceReference);

	public <T> void visitExternalSlotInstance(ExternalSlotInstanceReference<T> externalSlotInstanceReference);

	public <T> void visitServerBindingKeyInstanceReference(ServerBindingKeyInstanceReference<T> serverBindingKeyInstanceReference);

	public void visitFilledDataInstance(FilledDataInstanceReference filledDataInstanceReference);

	public void visitPendingDataInstance(PendingDataInstanceReference pendingDataInstanceReference);

	public void visitServerPendingDataInstance(ServerPendingDataInstanceReference serverPendingDataInstanceReference);

	public void visitDataObjectInstance(DataObjectInstanceReference dataObjectInstanceReference);

}
