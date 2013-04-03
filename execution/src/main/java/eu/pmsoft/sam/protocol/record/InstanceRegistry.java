package eu.pmsoft.sam.protocol.record;

import com.google.inject.Key;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.data.InstanceMergeVisitor;

import java.util.List;

public interface InstanceRegistry {

    // CONTEXT
    void setMethodRecordContext(MethodRecordContext context);

    // TRANSFER
    public List<CanonicalInstanceReference> getInstanceReferenceToTransfer();

    // INSTANCE CREATION
    public int createPendingDataBinding(Class<?> returnType);

    public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key);

    public <T> int createExternalInstanceBinding(Key<T> key, Object instanceProxy);

    public int createDataBinding(Object arg);

    // INSTANCE BINDING
    public void bindReturnObject(int returnInstance, Object returnObject);


    // INSTANCE INFORMATION
    public Object getInstanceObject(int instanceNumber);

    public Object[] getArguments(List<Integer> arguments);

    public CanonicalInstanceReference getInstanceReference(int instanceNr);

    // MERGE API
    public InstanceMergeVisitor getMergeVisitor();

}
