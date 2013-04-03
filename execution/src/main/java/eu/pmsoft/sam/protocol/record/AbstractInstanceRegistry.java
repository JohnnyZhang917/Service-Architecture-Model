package eu.pmsoft.sam.protocol.record;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Key;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.data.CanonicalProtocolSerialFactory;
import eu.pmsoft.sam.protocol.transport.data.InstanceMergeVisitor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkPositionIndex;

abstract class AbstractInstanceRegistry implements InstanceRegistry, InstanceMergeVisitor {

    protected MethodRecordContext executionContext;
    protected final int serviceSlotNr;
    protected final AtomicInteger internalInstanceCounter = new AtomicInteger(0);
    protected int transferedInstanceReferenceMark = 0;

    protected final ArrayList<CanonicalInstanceReference> instanceReferenceList = Lists.newArrayList();
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
        CanonicalInstanceReference pendingInstance = CanonicalProtocolSerialFactory.createPendingDataInstanceReference(serviceInstanceNr, returnType);
        addStackReferenceInstance(pendingInstance, null, serviceInstanceNr);
        return serviceInstanceNr;
    }

    protected void addStackReferenceInstance(CanonicalInstanceReference pendingInstance, Object reference, int serviceInstanceNr) {
        assert instanceReferenceList.size() == serviceInstanceNr;
        instanceReferenceList.add(pendingInstance);
        instanceObjectList.add(reference);
        assert instanceReferenceList.size() == instanceObjectList.size();
    }


    public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key) {
        int serviceInstanceNr = getNextInstanceNumber();
        CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(executionContext, key, serviceInstanceNr, serviceSlotNr);
        T instanceObject = recorder.getInstance();
        CanonicalInstanceReference bindingReference = CanonicalProtocolSerialFactory.createBindingKeyReference(serviceInstanceNr, key);
        addStackReferenceInstance(bindingReference, instanceObject, serviceInstanceNr);
        return recorder;
    }

    public <T> int createExternalInstanceBinding(Key<T> key, Object instanceProxy) {
        int serviceInstanceNr = getNextInstanceNumber();
        CanonicalInstanceReference externalInstance = CanonicalProtocolSerialFactory.createExternalInstanceReference(serviceInstanceNr, key);
        addStackReferenceInstance(externalInstance, instanceProxy, serviceInstanceNr);
        return serviceInstanceNr;
    }

    public Object getInstanceObject(int instanceNumber) {
        return instanceObjectList.get(instanceNumber);
    }

    public CanonicalInstanceReference getInstanceReference(int instanceNr) {
        checkPositionIndex(instanceNr, instanceReferenceList.size());
        return instanceReferenceList.get(instanceNr);
    }

    public void bindReturnObject(int returnInstance, Object returnObject) {
        checkPositionIndex(returnInstance, instanceReferenceList.size());
        CanonicalInstanceReference instance = instanceReferenceList.get(returnInstance);
        if (instance.getInstanceType() == CanonicalInstanceReference.InstanceType.PENDING_DATA_REF ||
                instance.getInstanceType() == CanonicalInstanceReference.InstanceType.SERVER_PENDING_DATA_REF) {
            // TODO FIXME if client and server pending data are separated, then maybe filleddata must be also separated
            CanonicalInstanceReference filledData = CanonicalProtocolSerialFactory.createFilledDataInstanceReference(returnInstance, returnObject);
            instanceReferenceList.set(returnInstance, filledData);
            instanceObjectList.set(returnInstance, returnObject);
            return;
        }
        if (instance.getInstanceType() == CanonicalInstanceReference.InstanceType.BINDING_KEY) {
            instanceObjectList.set(returnInstance, returnObject);
            return;
        }
        throw new RuntimeException("bind of returning object not possible, canonical protocol critical exceptions");

    }

    // TODO protostuff must give a int[]
    public Object[] getArguments(List<Integer> arguments) {
        int[] args = new int[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            args[i] = arguments.get(i);
        }
        return getArguments(args);
    }

    private Object[] getArguments(int[] arguments) {
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
        CanonicalInstanceReference reference = instanceReferenceList.get(returnInstance);
        Preconditions.checkState(reference.getInstanceType() == CanonicalInstanceReference.InstanceType.BINDING_KEY,
                "For this instance number there is not a BindingKeyInstanceReference reference, critical protocol exceptions.");
        instanceObjectList.set(returnInstance, returnObject);
    }

}
