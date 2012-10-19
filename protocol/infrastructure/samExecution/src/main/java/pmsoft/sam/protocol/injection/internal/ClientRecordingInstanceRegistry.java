package pmsoft.sam.protocol.injection.internal;

import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import pmsoft.sam.protocol.injection.internal.model.AbstractInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.BindingKeyInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.DataObjectInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.ExternalSlotInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.FilledDataInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.PendingDataInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.ServerBindingKeyInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.ServerPendingDataInstanceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ClientRecordingInstanceRegistry extends AbstractInstanceRegistry {

	public ClientRecordingInstanceRegistry(int serviceSlotNr) {
		super(serviceSlotNr);
	}

	public List<AbstractInstanceReference> getInstanceReferenceToTransfer() {
		Builder<AbstractInstanceReference> builder = ImmutableList.builder();
		for (int i = transferedInstanceReferenceMark; i < instanceReferenceList.size(); i++) {
			AbstractInstanceReference instanceRef = instanceReferenceList.get(i);
			if (instanceRef instanceof ServerPendingDataInstanceReference) {
				continue;
			}
			if (instanceRef instanceof ServerBindingKeyInstanceReference<?>) {
				continue;
			}
			builder.add(instanceRef);
		}
		transferedInstanceReferenceMark = instanceReferenceList.size();
		return builder.build();
	}

	@Override
	protected int getNextInstanceNumber() {
		return internalInstanceCounter.getAndIncrement();
	}

	@Override
	protected int getInstanceNumberPosition(int instanceNumber) {
		return instanceNumber;
	}

	public <T> void visitBindingKey(BindingKeyInstanceReference<T> bindingKeyInstanceReference) {
		throw new RuntimeException("not allowed type");
	}

	public <T> void visitExternalSlotInstance(ExternalSlotInstanceReference<T> externalSlotInstanceReference) {
		throw new RuntimeException("not allowed type");
	}
	
	public <T> void visitServerBindingKeyInstanceReference(ServerBindingKeyInstanceReference<T> serverBindingKeyInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(serverBindingKeyInstanceReference.getInstanceNr() == currentInstanceNumber);
		ServerBindingKeyInstanceReference<T> serverReference = new ServerBindingKeyInstanceReference<T>(currentInstanceNumber, serverBindingKeyInstanceReference.getKey());
		instanceReferenceList.add(serverReference);
		instanceObjectList.add(null);
	}
	
	public void visitServerPendingDataInstance(ServerPendingDataInstanceReference serverPendingDataInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(serverPendingDataInstanceReference.getInstanceNr() == currentInstanceNumber);
		PendingDataInstanceReference dataRef = new ServerPendingDataInstanceReference(currentInstanceNumber,
				serverPendingDataInstanceReference.getDataType());
		instanceReferenceList.add(dataRef);
		instanceObjectList.add(null);
	}

	public void visitFilledDataInstance(FilledDataInstanceReference filledDataInstanceReference) {
		int filledInstance = filledDataInstanceReference.getInstanceNr();
		int position = getInstanceNumberPosition(filledInstance);
		checkPositionIndex(position, instanceReferenceList.size());
		AbstractInstanceReference instance = instanceReferenceList.get(position);
		checkState(instance instanceof PendingDataInstanceReference,
				"A pending data position should be found, critical protocol error");
		Object objectReference = filledDataInstanceReference.getObjectReference();
		DataObjectInstanceReference dataRef = new DataObjectInstanceReference(filledInstance, objectReference );
		instanceReferenceList.set(position, dataRef);
		instanceObjectList.set(position,objectReference);
		
	}

	public void visitPendingDataInstance(PendingDataInstanceReference pendingDataInstanceReference) {
		throw new RuntimeException("not allowed type");	
	}

	public void visitDataObjectInstance(DataObjectInstanceReference dataObjectInstanceReference) {
		throw new RuntimeException("not allowed type");			
	}

}
