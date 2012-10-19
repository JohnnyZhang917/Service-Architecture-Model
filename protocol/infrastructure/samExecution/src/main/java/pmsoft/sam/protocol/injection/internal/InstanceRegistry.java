package pmsoft.sam.protocol.injection.internal;

import java.util.List;

import pmsoft.sam.protocol.injection.internal.model.AbstractInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.InstanceMergeVisitor;

import com.google.inject.Key;

public interface InstanceRegistry {

	// CONTEXT
//	void setRecordContext(ServiceSlotRecordingContext recordContext);
	void setMethodRecordContext(MethodRecordContext context);

	// TRANSFER
	public List<AbstractInstanceReference> getInstanceReferenceToTransfer();

	// INSTANCE CREATION
	public int createPendingDataBinding(Class<?> returnType);

	public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key);

	public <T> int createExternalInstanceBinding(Key<T> key, Object instanceProxy); 

	public int createDataBinding(Object arg);

	// INSTANCE BINDING
	public void bindReturnObject(int returnInstance, Object returnObject);

	
	// INSTANCE INFORMATION
	public Object getInstance(int instanceNumber);

	public Object[] getArguments(int[] arguments);

	public Object getDataInstance(int instanceNr);
	
	// MERGE API
	public InstanceMergeVisitor getMergeVisitor();


}
