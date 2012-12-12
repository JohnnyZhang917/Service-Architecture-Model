package pmsoft.sam.protocol.record;

import java.util.List;

import pmsoft.sam.protocol.transport.data.AbstractInstanceReference;
import pmsoft.sam.protocol.transport.data.InstanceMergeVisitor;

import com.google.inject.Key;

public interface InstanceRegistry {

	// CONTEXT
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
	public Object getInstanceObject(int instanceNumber);

	public Object[] getArguments(int[] arguments);

	public AbstractInstanceReference getInstanceReference(int instanceNr);
	
	// MERGE API
	public InstanceMergeVisitor getMergeVisitor();

}
