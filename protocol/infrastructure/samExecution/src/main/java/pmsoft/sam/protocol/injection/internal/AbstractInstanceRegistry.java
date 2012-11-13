package pmsoft.sam.protocol.injection.internal;

import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import pmsoft.sam.protocol.execution.model.AbstractInstanceReference;
import pmsoft.sam.protocol.execution.model.BindingKeyInstanceReference;
import pmsoft.sam.protocol.execution.model.ClientDataObjectInstanceReference;
import pmsoft.sam.protocol.execution.model.ExternalSlotInstanceReference;
import pmsoft.sam.protocol.execution.model.FilledDataInstanceReference;
import pmsoft.sam.protocol.execution.model.InstanceMergeVisitor;
import pmsoft.sam.protocol.execution.model.PendingDataInstanceReference;
import pmsoft.sam.protocol.execution.model.ServerDataObjectInstanceReference;
import pmsoft.sam.protocol.execution.model.ServerPendingDataInstanceReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Key;

public abstract class AbstractInstanceRegistry implements InstanceRegistry,InstanceMergeVisitor {

	protected MethodRecordContext executionContext;
	protected final int serviceSlotNr;
	protected final AtomicInteger internalInstanceCounter = new AtomicInteger(0);
	protected int transferedInstanceReferenceMark = 0;

	protected final ArrayList<AbstractInstanceReference> instanceReferenceList = Lists.newArrayList();
	protected final ArrayList<Object> instanceObjectList = Lists.newArrayList();

	public AbstractInstanceRegistry(int serviceSlotNr) {
		this.serviceSlotNr = serviceSlotNr;
	}
	
	@Override
	public void setMethodRecordContext(MethodRecordContext context) {
		this.executionContext = context;
	}

	public InstanceMergeVisitor getMergeVisitor() {
		return this;
	}

	protected abstract int getNextInstanceNumber();
	
	protected abstract int getInstanceNumberPosition(int instanceNumber) ;

	public int createPendingDataBinding(Class<?> returnType) {
		int serviceInstanceNr = getNextInstanceNumber();
		PendingDataInstanceReference pendingInstance = new PendingDataInstanceReference(serviceInstanceNr, returnType);
		instanceReferenceList.add(pendingInstance);
		instanceObjectList.add(null);
		return serviceInstanceNr;
	}

	public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key) {
		int serviceInstanceNr = getNextInstanceNumber();
		CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(executionContext, key, serviceInstanceNr,
				serviceSlotNr);
		T instanceObject = recorder.getInstance();
		BindingKeyInstanceReference<T> bindingReference = new BindingKeyInstanceReference<T>(serviceInstanceNr, key);
		instanceReferenceList.add(bindingReference);
		instanceObjectList.add(instanceObject);
		return recorder;
	}

	public <T> int createExternalInstanceBinding(Key<T> key, Object instanceProxy) {
		int serviceInstanceNr = getNextInstanceNumber();
		ExternalSlotInstanceReference<T> externalInstance = new ExternalSlotInstanceReference<T>(serviceInstanceNr, key);
		instanceReferenceList.add(externalInstance);
		instanceObjectList.add(instanceProxy);
		return serviceInstanceNr;
	}

	public Object getInstance(int instanceNumber) {
		int instancePosition = getInstanceNumberPosition(instanceNumber);
		return instanceObjectList.get(instancePosition);
	}

	public Object getDataInstance(int instanceNr) {
		int position = getInstanceNumberPosition(instanceNr);
		AbstractInstanceReference reference = instanceReferenceList.get(position);
		checkState(reference instanceof ClientDataObjectInstanceReference || reference instanceof ServerDataObjectInstanceReference );
		return instanceObjectList.get(position);
	}
	
	public void bindReturnObject(int returnInstance, Object returnObject) {
		int instancePosition = getInstanceNumberPosition(returnInstance);
		checkPositionIndex(instancePosition, instanceReferenceList.size());
		AbstractInstanceReference instance = instanceReferenceList.get(instancePosition);
		if (instance instanceof PendingDataInstanceReference || instance instanceof ServerPendingDataInstanceReference) {
			instanceReferenceList.set(instancePosition, new FilledDataInstanceReference(returnInstance, returnObject));
			instanceObjectList.set(instancePosition,returnObject);
			return;
		}
		if (instance instanceof BindingKeyInstanceReference) {
			instanceObjectList.set(returnInstance,returnObject);
			return;
		}
		throw new RuntimeException("bind of returning object not possible, canonical protocol critical error");
		
	}
	
	public Object[] getArguments(int[] arguments) {
		Object[] argObjets = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			int instanceNumber = arguments[i];
			int position = getInstanceNumberPosition(instanceNumber);
			if( position >=0) {
				argObjets[i] = instanceObjectList.get(position);
			}
		}
		return argObjets;
	}
	
	protected void fillInstanceKeyReference(int returnInstance, Object returnObject) {
		int instancePosition = getInstanceNumberPosition(returnInstance);
		AbstractInstanceReference reference = instanceReferenceList.get(instancePosition);
		Preconditions.checkState(reference instanceof BindingKeyInstanceReference,
				"For this instance number there is not a BindingKeyInstanceReference reference, critical protocol error.");	
		instanceObjectList.set(returnInstance,returnObject);
	}

}
