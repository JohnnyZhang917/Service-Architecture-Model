package pmsoft.sam.protocol.record;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import pmsoft.sam.protocol.transport.data.AbstractInstanceReference;
import pmsoft.sam.protocol.transport.data.BindingKeyInstanceReference;
import pmsoft.sam.protocol.transport.data.ClientDataObjectInstanceReference;
import pmsoft.sam.protocol.transport.data.ExternalSlotInstanceReference;
import pmsoft.sam.protocol.transport.data.FilledDataInstanceReference;
import pmsoft.sam.protocol.transport.data.PendingDataInstanceReference;
import pmsoft.sam.protocol.transport.data.ServerBindingKeyInstanceReference;
import pmsoft.sam.protocol.transport.data.ServerDataObjectInstanceReference;
import pmsoft.sam.protocol.transport.data.ServerPendingDataInstanceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Injector;
import com.google.inject.Key;
import pmsoft.sam.see.api.model.SamServiceInstance;

class ServerExecutionInstanceRegistry extends AbstractInstanceRegistry {

	private final Injector realServiceInjector;
    private int pendingDataTransferReferenceMark = 0;
    private static final int SERVER_SLOT_NR = 0;

    @Inject
	ServerExecutionInstanceRegistry(@Assisted SamServiceInstance serviceInstance) {
		super(SERVER_SLOT_NR);
        this.realServiceInjector = serviceInstance.getInjector();
	}

	public List<AbstractInstanceReference> getInstanceReferenceToTransfer() {
		Builder<AbstractInstanceReference> builder = ImmutableList.builder();
		int position = transferedInstanceReferenceMark;
		for (int i = transferedInstanceReferenceMark; i < instanceReferenceList.size(); i++) {
			AbstractInstanceReference instanceRef = instanceReferenceList.get(position++);
			if (instanceRef instanceof ServerPendingDataInstanceReference) {
				builder.add(instanceRef);
				break;
			}
			if (instanceRef instanceof ServerBindingKeyInstanceReference<?>) {
				builder.add(instanceRef);
			}
			if (instanceRef instanceof ServerDataObjectInstanceReference) {
				builder.add(instanceRef);
			}
		}
		transferedInstanceReferenceMark = position;

		position = pendingDataTransferReferenceMark;
		for (int i = pendingDataTransferReferenceMark; i < instanceReferenceList.size(); i++) {
			AbstractInstanceReference instanceRef = instanceReferenceList.get(position++);
			if (instanceRef instanceof PendingDataInstanceReference) {
				position--;
				break;
			}
			if (instanceRef instanceof FilledDataInstanceReference) {
				builder.add(instanceRef);
			}

		}
		pendingDataTransferReferenceMark = position;
		return builder.build();
	}

	@Override
	public int createPendingDataBinding(Class<?> returnType) {
		int serviceInstanceNr = getNextInstanceNumber();
		ServerPendingDataInstanceReference pendingInstance = new ServerPendingDataInstanceReference(serviceInstanceNr, returnType);
		instanceReferenceList.add(pendingInstance);
		instanceObjectList.add(null);
		return serviceInstanceNr;
	}
	
	public int createDataBinding(Object arg) {
		int serviceInstanceNr = getNextInstanceNumber();
		ServerDataObjectInstanceReference dataInstance = new ServerDataObjectInstanceReference(serviceInstanceNr, arg);
		instanceReferenceList.add(dataInstance);
		instanceObjectList.add(arg);
		return serviceInstanceNr;
	}

	@Override
	public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key) {
		int serviceInstanceNr = getNextInstanceNumber();
		CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(executionContext, key, serviceInstanceNr, serviceSlotNr);
		T instanceObject = recorder.getInstance();
		BindingKeyInstanceReference<T> bindingReference = new ServerBindingKeyInstanceReference<T>(serviceInstanceNr, key);
		instanceReferenceList.add(bindingReference);
		instanceObjectList.add(instanceObject);
		return recorder;
	}

	@Override
	public Object getInstanceObject(int instanceNumber) {
		Object existingInstance = super.getInstanceObject(instanceNumber);
		if (existingInstance == null) {
			// Check if this is a BindingKeyInstance that should be created
			AbstractInstanceReference reference = instanceReferenceList.get(instanceNumber);
			if (reference instanceof BindingKeyInstanceReference) {

				BindingKeyInstanceReference<?> keyReference = (BindingKeyInstanceReference<?>) reference;
				Key<?> key = keyReference.getKey();
				checkArgument(realServiceInjector.getExistingBinding(key) != null,
						"A Key binding not exposed by the service has not been filled by any previous method call. Canonical protocol critical exceptions");
				existingInstance = realServiceInjector.getInstance(key);
				fillInstanceKeyReference(instanceNumber, existingInstance);
			} else {
				throw new RuntimeException("Instance creation is not possible, canonical protocol critical exceptions");
			}
		}
		return existingInstance;
	}

	@Override
	protected int getNextInstanceNumber() {
		return internalInstanceCounter.getAndIncrement();
	}

	public <T> void visitBindingKey(BindingKeyInstanceReference<T> bindingKeyInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(bindingKeyInstanceReference.getInstanceNr() == currentInstanceNumber);
		BindingKeyInstanceReference<T> bindingReference = new BindingKeyInstanceReference<T>(currentInstanceNumber, bindingKeyInstanceReference.getKey());
		instanceReferenceList.add(bindingReference);
		// Do not create the instance, because this keyBinding can be the result
		// of a local method execution.
		// NOT -> instanceObjectList.add(injector.getInstance(key));
		instanceObjectList.add(null);
	}

	public <T> void visitExternalSlotInstance(ExternalSlotInstanceReference<T> externalSlotInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(externalSlotInstanceReference.getInstanceNr() == currentInstanceNumber);
		Key<T> key = externalSlotInstanceReference.getKey();
		CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(executionContext, key, currentInstanceNumber, SERVER_SLOT_NR);
		ExternalSlotInstanceReference<T> reference = new ExternalSlotInstanceReference<T>(currentInstanceNumber, key);
		instanceReferenceList.add(reference);
		instanceObjectList.add(recorder.getInstance());
	}

	public <T> void visitServerBindingKeyInstanceReference(ServerBindingKeyInstanceReference<T> serverBindingKeyInstanceReference) {
		throw new RuntimeException("not allowed type");
	}

	public void visitFilledDataInstance(FilledDataInstanceReference filledDataInstanceReference) {
		int position = filledDataInstanceReference.getInstanceNr();
		checkPositionIndex(position, instanceReferenceList.size());
		AbstractInstanceReference instance = instanceReferenceList.get(position);
		checkState(instance instanceof ServerPendingDataInstanceReference, "A server pending data position should be found, critical protocol exceptions");
		Object objectReference = filledDataInstanceReference.getObjectReference();
		ClientDataObjectInstanceReference dataRef = new ClientDataObjectInstanceReference(position, objectReference);
		instanceReferenceList.set(position, dataRef);
		instanceObjectList.set(position, objectReference);
	}

	public void visitServerPendingDataInstance(ServerPendingDataInstanceReference serverPendingDataInstanceReference) {
		throw new RuntimeException("not allowed type");
	}

	public void visitPendingDataInstance(PendingDataInstanceReference pendingDataInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(pendingDataInstanceReference.getInstanceNr() == currentInstanceNumber);
		PendingDataInstanceReference dataRef = new PendingDataInstanceReference(currentInstanceNumber, pendingDataInstanceReference.getDataType());
		instanceReferenceList.add(dataRef);
		instanceObjectList.add(null);
	}

	@Override
	public void visitClientDataObjectInstance(ClientDataObjectInstanceReference dataObjectInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(dataObjectInstanceReference.getInstanceNr() == currentInstanceNumber);
		Object objectReference = dataObjectInstanceReference.getObjectReference();
		ClientDataObjectInstanceReference dataRef = new ClientDataObjectInstanceReference(currentInstanceNumber, objectReference);
		instanceReferenceList.add(dataRef);
		instanceObjectList.add(objectReference);
	}

	@Override
	public void visitServerDataObjectInstance(ServerDataObjectInstanceReference dataObjectInstanceReference) {
		throw new RuntimeException("not allowed type");
	}
	
}
