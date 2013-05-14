package eu.pmsoft.sam.protocol.record;

import com.dyuproject.protostuff.ByteString;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.data.CanonicalProtocolSerialFactory;
import eu.pmsoft.sam.protocol.transport.data.SerializableKey;
import eu.pmsoft.see.api.model.SamServiceInstance;

import java.util.List;

import static com.google.common.base.Preconditions.*;

class ServerExecutionInstanceRegistryDeprecated extends AbstractInstanceRegistryDeprecated {

    private final Injector realServiceInjector;
    private int pendingDataTransferReferenceMark = 0;
    private static final int SERVER_SLOT_NR = 0;

    @Inject
    ServerExecutionInstanceRegistryDeprecated(@Assisted SamServiceInstance serviceInstance) {
        super(SERVER_SLOT_NR);
        this.realServiceInjector = serviceInstance.getInjector();
    }

    public List<CanonicalInstanceReference> getInstanceReferenceToTransfer() {
        Builder<CanonicalInstanceReference> builder = ImmutableList.builder();
        int position = transferedInstanceReferenceMark;
        for (int i = transferedInstanceReferenceMark; i < instanceReferenceList.size(); i++) {
            CanonicalInstanceReference instanceRef = instanceReferenceList.get(position++);
            if (instanceRef.getInstanceType() == CanonicalInstanceReference.InstanceType.SERVER_PENDING_DATA_REF) {
                builder.add(instanceRef);
                break;
            }
            if (instanceRef.getInstanceType() == CanonicalInstanceReference.InstanceType.SERVER_BINDING_KEY) {
                builder.add(instanceRef);
            }
            if (instanceRef.getInstanceType() == CanonicalInstanceReference.InstanceType.SERVER_DATA_OBJECT) {
                builder.add(instanceRef);
            }
        }
        transferedInstanceReferenceMark = position;

        position = pendingDataTransferReferenceMark;
        for (int i = pendingDataTransferReferenceMark; i < instanceReferenceList.size(); i++) {
            CanonicalInstanceReference instanceRef = instanceReferenceList.get(position++);
            if (instanceRef.getInstanceType() == CanonicalInstanceReference.InstanceType.PENDING_DATA_REF) {
                position--;
                break;
            }
            if (instanceRef.getInstanceType() == CanonicalInstanceReference.InstanceType.FILLED_DATA_INSTANCE) {
                builder.add(instanceRef);
            }

        }
        pendingDataTransferReferenceMark = position;
        return builder.build();
    }

    @Override
    public int createPendingDataBinding(Class<?> returnType) {
        int serviceInstanceNr = getNextInstanceNumber();
        CanonicalInstanceReference pendingInstance = CanonicalProtocolSerialFactory.createServerPendingDataInstanceReference(serviceInstanceNr, returnType);
        instanceReferenceList.add(pendingInstance);
        instanceObjectList.add(null);
        return serviceInstanceNr;
    }

    public int createDataBinding(Object arg) {
        int serviceInstanceNr = getNextInstanceNumber();
        CanonicalInstanceReference dataInstance = CanonicalProtocolSerialFactory.createServerDataInstanceReference(serviceInstanceNr, arg);
        instanceReferenceList.add(dataInstance);
        instanceObjectList.add(arg);
        return serviceInstanceNr;
    }

    @Override
    public <T> CanonicalInstanceRecorder<T> createKeyBinding(Key<T> key) {
        int serviceInstanceNr = getNextInstanceNumber();
        CanonicalInstanceRecorder<T> recorder = new CanonicalInstanceRecorder<T>(executionContext, key, serviceInstanceNr, serviceSlotNr);
        T instanceObject = recorder.getInstance();
        CanonicalInstanceReference bindingReference = CanonicalProtocolSerialFactory.createServerBindingKeyInstanceReference(serviceInstanceNr, key);
        instanceReferenceList.add(bindingReference);
        instanceObjectList.add(instanceObject);
        return recorder;
    }

    @Override
    public Object getInstanceObject(int instanceNumber) {
        Object existingInstance = super.getInstanceObject(instanceNumber);
        if (existingInstance == null) {
            // Check if this is a BindingKeyInstance that should be created
            CanonicalInstanceReference reference = instanceReferenceList.get(instanceNumber);
            if (reference != null && reference.getInstanceType() == CanonicalInstanceReference.InstanceType.BINDING_KEY) {
                Key<?> key = SerializableKey.toKey(reference.getKey());
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

    @Override
    public void merge(CanonicalInstanceReference instanceReference) {
        CanonicalInstanceReference.InstanceType instanceType = instanceReference.getInstanceType();
        int currentInstanceNumber;
        switch (instanceType) {
            case SERVER_BINDING_KEY:
            case SERVER_PENDING_DATA_REF:
            case SERVER_DATA_OBJECT:
                throw new RuntimeException("not allowed type on server site");
            case BINDING_KEY:
                currentInstanceNumber = getNextInstanceNumber();
                checkState(instanceReference.getInstanceNr() == currentInstanceNumber);
                CanonicalInstanceReference bindingReference = CanonicalProtocolSerialFactory.createBindingKeyReference(currentInstanceNumber, instanceReference.getKey());
                instanceReferenceList.add(bindingReference);
                // Do not create the instance, because this keyBinding can be the result of a local method execution.
                // NOT -> instanceObjectList.add(injector.getInstance(key));
                // see method getInstanceObject
                instanceObjectList.add(null);
                break;
            case EXTERNAL_INSTANCE_REFERENCE:
                currentInstanceNumber = getNextInstanceNumber();
                checkState(instanceReference.getInstanceNr() == currentInstanceNumber);
                Key<?> key = SerializableKey.toKey(instanceReference.getKey());
                CanonicalInstanceRecorder<?> recorder = new CanonicalInstanceRecorder(executionContext, key, currentInstanceNumber, SERVER_SLOT_NR);
                CanonicalInstanceReference reference = CanonicalProtocolSerialFactory.createExternalInstanceReference(currentInstanceNumber, key);
                instanceReferenceList.add(reference);
                instanceObjectList.add(recorder.getInstance());
                break;
            case PENDING_DATA_REF:
                currentInstanceNumber = getNextInstanceNumber();
                checkState(instanceReference.getInstanceNr() == currentInstanceNumber);
                CanonicalInstanceReference dataRef = CanonicalProtocolSerialFactory.createPendingDataInstanceReference(currentInstanceNumber, instanceReference.getDataType());
                instanceReferenceList.add(dataRef);
                instanceObjectList.add(null);
                break;
            case CLIENT_DATA_OBJECT:
                currentInstanceNumber = getNextInstanceNumber();
                checkState(instanceReference.getInstanceNr() == currentInstanceNumber);
                ByteString objectReferenceClientData = instanceReference.getDataObjectSerialization();
                CanonicalInstanceReference clientDataRef = CanonicalProtocolSerialFactory.createClientDataObject(currentInstanceNumber, objectReferenceClientData);
                Object objectReferenceInstance = CanonicalProtocolSerialFactory.deserialize(objectReferenceClientData);
                instanceReferenceList.add(clientDataRef);
                instanceObjectList.add(objectReferenceInstance);
                break;
            case FILLED_DATA_INSTANCE:
                int position = instanceReference.getInstanceNr();
                checkPositionIndex(position, instanceReferenceList.size());
                CanonicalInstanceReference instance = instanceReferenceList.get(position);
                checkState(instance.getInstanceType() == CanonicalInstanceReference.InstanceType.SERVER_PENDING_DATA_REF, "A server pending data position should be found, critical protocol exceptions");
                ByteString objectReferenceFilledData = instanceReference.getDataObjectSerialization();
                CanonicalInstanceReference filledDataRef = CanonicalProtocolSerialFactory.createClientDataObject(position, objectReferenceFilledData);
                Object objectReferenceFilledInstance = CanonicalProtocolSerialFactory.deserialize(objectReferenceFilledData);
                instanceReferenceList.set(position, filledDataRef);
                instanceObjectList.set(position, objectReferenceFilledInstance);
                break;
        }
    }

}
