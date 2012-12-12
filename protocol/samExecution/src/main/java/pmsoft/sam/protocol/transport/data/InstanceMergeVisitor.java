package pmsoft.sam.protocol.transport.data;


public interface InstanceMergeVisitor {

	public <T> void visitBindingKey(BindingKeyInstanceReference<T> bindingKeyInstanceReference);

	public <T> void visitExternalSlotInstance(ExternalSlotInstanceReference<T> externalSlotInstanceReference);

	public <T> void visitServerBindingKeyInstanceReference(ServerBindingKeyInstanceReference<T> serverBindingKeyInstanceReference);

	public void visitFilledDataInstance(FilledDataInstanceReference filledDataInstanceReference);

	public void visitPendingDataInstance(PendingDataInstanceReference pendingDataInstanceReference);

	public void visitServerPendingDataInstance(ServerPendingDataInstanceReference serverPendingDataInstanceReference);

	public void visitClientDataObjectInstance(ClientDataObjectInstanceReference dataObjectInstanceReference);
	
	public void visitServerDataObjectInstance(ServerDataObjectInstanceReference dataObjectInstanceReference);

}
