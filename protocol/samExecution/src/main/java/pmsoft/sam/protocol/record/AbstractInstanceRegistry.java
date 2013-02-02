package pmsoft.sam.protocol.record;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Key;
import org.slf4j.Logger;
import pmsoft.injectionUtils.logger.InjectLogger;
import pmsoft.sam.protocol.transport.data.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkPositionIndex;

abstract class AbstractInstanceRegistry implements InstanceRegistry, InstanceMergeVisitor {

    protected MethodRecordContext executionContext;
    protected final int serviceSlotNr;
    protected final AtomicInteger internalInstanceCounter = new AtomicInteger(0);
    protected int transferedInstanceReferenceMark = 0;

    protected final ArrayList<AbstractInstanceReference> instanceReferenceList = Lists.newArrayList();
    protected final ArrayList<Object> instanceObjectList = Lists.newArrayList();
    @InjectLogger
    protected Logger logger;

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

    public int createPendingDataBinding(Class<?> returnType) {
        int serviceInstanceNr = getNextInstanceNumber();
        PendingDataInstanceReference pendingInstance = new PendingDataInstanceReference(serviceInstanceNr, returnType);
        instanceReferenceList.add(pendingInstance);
        instanceObjectList.add(null);
        return serviceInstanceNr;
    }

    public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key) {
        int serviceInstanceNr = getNextInstanceNumber();
        CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(executionContext, key, serviceInstanceNr, serviceSlotNr);
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

    public Object getInstanceObject(int instanceNumber) {
        return instanceObjectList.get(instanceNumber);
    }

    public AbstractInstanceReference getInstanceReference(int instanceNr) {
        checkPositionIndex(instanceNr, instanceReferenceList.size());
        return instanceReferenceList.get(instanceNr);
    }

    public void bindReturnObject(int returnInstance, Object returnObject) {
        checkPositionIndex(returnInstance, instanceReferenceList.size());
        AbstractInstanceReference instance = instanceReferenceList.get(returnInstance);
        if (instance instanceof PendingDataInstanceReference || instance instanceof ServerPendingDataInstanceReference) {
            instanceReferenceList.set(returnInstance, new FilledDataInstanceReference(returnInstance, returnObject));
            instanceObjectList.set(returnInstance, returnObject);
            return;
        }
        if (instance instanceof BindingKeyInstanceReference) {
            instanceObjectList.set(returnInstance, returnObject);
            return;
        }
        throw new RuntimeException("bind of returning object not possible, canonical protocol critical exceptions");

    }

    public Object[] getArguments(int[] arguments) {
        Object[] argObjets = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            int instanceNumber = arguments[i];
            if (instanceNumber >= 0) {
                argObjets[i] = instanceObjectList.get(instanceNumber);
            }
        }
        return argObjets;
    }

    protected void fillInstanceKeyReference(int returnInstance, Object returnObject) {
        AbstractInstanceReference reference = instanceReferenceList.get(returnInstance);
        Preconditions.checkState(reference instanceof BindingKeyInstanceReference,
                "For this instance number there is not a BindingKeyInstanceReference reference, critical protocol exceptions.");
        instanceObjectList.set(returnInstance, returnObject);
    }

}
