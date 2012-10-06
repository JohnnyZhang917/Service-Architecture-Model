package pmsoft.sam.canonical.deprecated.api.ir;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import pmsoft.sam.canonical.deprecated.CanonicalInstanceRecorder;
import pmsoft.sam.canonical.deprecated.api.ServiceSlotRecordingContext;
import pmsoft.sam.canonical.deprecated.api.ir.model.AbstractInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.BindingKeyInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.DataObjectInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.ExternalSlotInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.FilledDataInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.PendingDataInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.ServerBindingKeyInstanceReference;
import pmsoft.sam.canonical.deprecated.api.ir.model.ServerPendingDataInstanceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Injector;
import com.google.inject.Key;

public class ServerExecutionInstanceRegistry extends AbstractInstanceRegistry {

	private final Injector realServiceInjector;
	private int pendingDataTransferReferenceMark = 0;

	public ServerExecutionInstanceRegistry(ServiceSlotRecordingContext recordContext, Injector realServiceInjector) {
		super(recordContext, 0);
		this.realServiceInjector = realServiceInjector;
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
	
	@Override
	public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key) {
		int serviceInstanceNr = getNextInstanceNumber();
		CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(recordContext, key, serviceInstanceNr,
				serviceSlotNr);
		T instanceObject = recorder.getInstance();
		BindingKeyInstanceReference<T> bindingReference = new ServerBindingKeyInstanceReference<T>(serviceInstanceNr, key);
		instanceReferenceList.add(bindingReference);
		instanceObjectList.add(instanceObject);
		return recorder;		
	}

	@Override
	public Object getInstance(int instanceNumber) {
		Object existingInstance = super.getInstance(instanceNumber);
		if (existingInstance == null) {
			// Check if this is a BindingKeyInstance that should be created
			AbstractInstanceReference reference = instanceReferenceList.get(instanceNumber);
			if (reference instanceof BindingKeyInstanceReference) {

				BindingKeyInstanceReference<?> keyReference = (BindingKeyInstanceReference<?>) reference;
				Key<?> key = keyReference.getKey();
				checkArgument(realServiceInjector.getExistingBinding(key) != null,
						"A Key binding not exposed by the service has not been filled by any previous method call. Canonical protocol critical error");
				existingInstance = realServiceInjector.getInstance(key);
				fillInstanceKeyReference(instanceNumber, existingInstance);
			} else {
				throw new RuntimeException("Instance creation is not possible, canonical protocol critical error");
			}
		}
		return existingInstance;
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
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(bindingKeyInstanceReference.getInstanceNr() == currentInstanceNumber);
		BindingKeyInstanceReference<T> bindingReference = new BindingKeyInstanceReference<T>(currentInstanceNumber,
				bindingKeyInstanceReference.getKey());
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
		CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(recordContext, key, currentInstanceNumber, 1);
		ExternalSlotInstanceReference<T> reference = new ExternalSlotInstanceReference<T>(currentInstanceNumber, key);
		instanceReferenceList.add(reference);
		instanceObjectList.add(recorder.getInstance());
	}

	public <T> void visitServerBindingKeyInstanceReference(ServerBindingKeyInstanceReference<T> serverBindingKeyInstanceReference) {
		throw new RuntimeException("not allowed type");
	}

	public void visitFilledDataInstance(FilledDataInstanceReference filledDataInstanceReference) {
		int filledInstance = filledDataInstanceReference.getInstanceNr();
		int position = getInstanceNumberPosition(filledInstance);
		checkPositionIndex(position, instanceReferenceList.size());
		AbstractInstanceReference instance = instanceReferenceList.get(position);
		checkState(instance instanceof ServerPendingDataInstanceReference,
				"A server pending data position should be found, critical protocol error");
		Object objectReference = filledDataInstanceReference.getObjectReference();
		DataObjectInstanceReference dataRef = new DataObjectInstanceReference(filledInstance, objectReference );
		instanceReferenceList.set(position, dataRef);
		instanceObjectList.set(position,objectReference);
	}

	public void visitServerPendingDataInstance(ServerPendingDataInstanceReference serverPendingDataInstanceReference) {
		throw new RuntimeException("not allowed type");
	}

	public void visitPendingDataInstance(PendingDataInstanceReference pendingDataInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(pendingDataInstanceReference.getInstanceNr() == currentInstanceNumber);
		PendingDataInstanceReference dataRef = new PendingDataInstanceReference(currentInstanceNumber,
				pendingDataInstanceReference.getDataType());
		instanceReferenceList.add(dataRef);
		instanceObjectList.add(null);
	}

	public void visitDataObjectInstance(DataObjectInstanceReference dataObjectInstanceReference) {
		int currentInstanceNumber = getNextInstanceNumber();
		checkState(dataObjectInstanceReference.getInstanceNr() == currentInstanceNumber);
		Object objectReference = dataObjectInstanceReference.getObjectReference();
		DataObjectInstanceReference dataRef = new DataObjectInstanceReference(currentInstanceNumber, objectReference);
		instanceReferenceList.add(dataRef);
		instanceObjectList.add(objectReference);
	}

}
