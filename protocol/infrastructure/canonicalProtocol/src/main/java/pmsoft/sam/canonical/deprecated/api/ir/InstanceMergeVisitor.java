package pmsoft.sam.canonical.deprecated.api.ir;

import pmsoft.sam.canonical.deprecated.api.ir.model.BindingKeyInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.DataObjectInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.ExternalSlotInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.FilledDataInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.PendingDataInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.ServerBindingKeyInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.ServerPendingDataInstanceReference;

public interface InstanceMergeVisitor {

	public <T> void visitBindingKey(BindingKeyInstanceReference<T> bindingKeyInstanceReference);

	public <T> void visitExternalSlotInstance(ExternalSlotInstanceReference<T> externalSlotInstanceReference);

	public <T> void visitServerBindingKeyInstanceReference(ServerBindingKeyInstanceReference<T> serverBindingKeyInstanceReference);

	public void visitFilledDataInstance(FilledDataInstanceReference filledDataInstanceReference);

	public void visitPendingDataInstance(PendingDataInstanceReference pendingDataInstanceReference);

	public void visitServerPendingDataInstance(ServerPendingDataInstanceReference serverPendingDataInstanceReference);

	public void visitDataObjectInstance(DataObjectInstanceReference dataObjectInstanceReference);

}
