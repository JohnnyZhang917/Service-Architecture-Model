package pmsoft.sam.protocol.record;

import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import pmsoft.sam.protocol.transport.data.AbstractInstanceReference;
import pmsoft.sam.protocol.transport.data.BindingKeyInstanceReference;
import pmsoft.sam.protocol.transport.data.ClientDataObjectInstanceReference;
import pmsoft.sam.protocol.transport.data.ExternalSlotInstanceReference;
import pmsoft.sam.protocol.transport.data.FilledDataInstanceReference;
import pmsoft.sam.protocol.transport.data.PendingDataInstanceReference;
import pmsoft.sam.protocol.transport.data.ServerBindingKeyInstanceReference;
import pmsoft.sam.protocol.transport.data.ServerDataObjectInstanceReference;
import pmsoft.sam.protocol.transport.data.ServerPendingDataInstanceReference;
import pmsoft.sam.see.api.model.SIURL;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;

class ClientRecordingInstanceRegistry extends AbstractInstanceRegistry implements InstanceProvider, InstanceRegistry {

    @Inject
	public ClientRecordingInstanceRegistry(@Assisted int serviceSlotNr) {
		super(serviceSlotNr);
    }

	public <T> T getInstance(Key<T> key) {
		return createKeyBinding(key).getInstance();
	}
	
	public int createDataBinding(Object arg) {
		int serviceInstanceNr = getNextInstanceNumber();
		ClientDataObjectInstanceReference dataInstance = new ClientDataObjectInstanceReference(serviceInstanceNr, arg);
		instanceReferenceList.add(dataInstance);
		instanceObjectList.add(arg);
		return serviceInstanceNr;
	}

	public List<AbstractInstanceReference> getInstanceReferenceToTransfer() {
		Builder<AbstractInstanceReference> builder = ImmutableList.builder();
		for (int i = transferedInstanceReferenceMark; i < instanceReferenceList.size(); i++) {
			AbstractInstanceReference instanceRef = instanceReferenceList.get(i);
            if (instanceRef instanceof ServerPendingDataInstanceReference) {
                continue;
            }
            if (instanceRef instanceof ServerDataObjectInstanceReference) {
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
		int position = filledDataInstanceReference.getInstanceNr();
		checkPositionIndex(position, instanceReferenceList.size());
		AbstractInstanceReference instance = instanceReferenceList.get(position);
		checkState(instance instanceof PendingDataInstanceReference,
				"A pending data position should be found, critical protocol exceptions");
		Object objectReference = filledDataInstanceReference.getObjectReference();
        FilledDataInstanceReference dataRef = new FilledDataInstanceReference(position, objectReference );
		instanceReferenceList.set(position, dataRef);
		instanceObjectList.set(position,objectReference);
		
	}

	public void visitPendingDataInstance(PendingDataInstanceReference pendingDataInstanceReference) {
		throw new RuntimeException("not allowed type");	
	}

	@Override
	public void visitClientDataObjectInstance(ClientDataObjectInstanceReference dataObjectInstanceReference) {
		throw new RuntimeException("not allowed type");			
	}
	
	@Override
	public void visitServerDataObjectInstance(ServerDataObjectInstanceReference dataObjectInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(dataObjectInstanceReference.getInstanceNr() == currentInstanceNumber, "exceptions on merge of %s. currentInstanceNumber = %s", dataObjectInstanceReference,currentInstanceNumber);
		Object objectReference = dataObjectInstanceReference.getObjectReference();
		ServerDataObjectInstanceReference dataRef = new ServerDataObjectInstanceReference(currentInstanceNumber, objectReference);
		instanceReferenceList.add(dataRef);
		instanceObjectList.add(objectReference);
	}

}
