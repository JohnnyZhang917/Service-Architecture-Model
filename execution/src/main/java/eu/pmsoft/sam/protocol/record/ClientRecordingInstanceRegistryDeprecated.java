package eu.pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.data.CanonicalProtocolSerialFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

class ClientRecordingInstanceRegistryDeprecated extends AbstractInstanceRegistryDeprecated implements InstanceProvider, InstanceRegistry {

    @Inject
    public ClientRecordingInstanceRegistryDeprecated(@Assisted int serviceSlotNr) {
        super(serviceSlotNr);
    }

    public <T> T getInstance(Key<T> key) {
        return createKeyBinding(key).getInstance();
    }

    public int createDataBinding(Object arg) {
        int serviceInstanceNr = getNextInstanceNumber();
        CanonicalInstanceReference dataInstance = CanonicalProtocolSerialFactory.createClientDataObjectSerializable(serviceInstanceNr, arg);
        addStackReferenceInstance(dataInstance, arg, serviceInstanceNr);
        return serviceInstanceNr;
    }


    public List<CanonicalInstanceReference> getInstanceReferenceToTransfer() {
        Builder<CanonicalInstanceReference> builder = ImmutableList.builder();
        for (int i = transferedInstanceReferenceMark; i < instanceReferenceList.size(); i++) {
            CanonicalInstanceReference instanceRef = instanceReferenceList.get(i);
            switch (instanceRef.getInstanceType()) {
                case SERVER_PENDING_DATA_REF:
                case SERVER_DATA_OBJECT:
                case SERVER_BINDING_KEY:
                    continue;
                case BINDING_KEY:
                case EXTERNAL_INSTANCE_REFERENCE:
                case PENDING_DATA_REF:
                case FILLED_DATA_INSTANCE:
                case CLIENT_DATA_OBJECT:
                    builder.add(instanceRef);
                    break;
            }
        }
        transferedInstanceReferenceMark = instanceReferenceList.size();
        return builder.build();
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
            case BINDING_KEY:
            case EXTERNAL_INSTANCE_REFERENCE:
            case PENDING_DATA_REF:
            case CLIENT_DATA_OBJECT:
                throw new RuntimeException("not allowed type on client site");
            case SERVER_BINDING_KEY:
                currentInstanceNumber = getNextInstanceNumber();
                checkState(instanceReference.getInstanceNr() == currentInstanceNumber);
                CanonicalInstanceReference serverReference = CanonicalProtocolSerialFactory.createServerBindingKeyInstanceReference(currentInstanceNumber, instanceReference.getKey());
                addStackReferenceInstance(serverReference, null, currentInstanceNumber);
                break;
            case SERVER_PENDING_DATA_REF:
                currentInstanceNumber = getNextInstanceNumber();
                checkState(instanceReference.getInstanceNr() == currentInstanceNumber);
                CanonicalInstanceReference dataPendingRef = CanonicalProtocolSerialFactory.createServerPendingDataInstanceReference(currentInstanceNumber, instanceReference.getDataType());
                addStackReferenceInstance(dataPendingRef, null, currentInstanceNumber);
                break;
            case FILLED_DATA_INSTANCE:
                int position = instanceReference.getInstanceNr();
                checkPositionIndex(position, instanceReferenceList.size());
                CanonicalInstanceReference instance = instanceReferenceList.get(position);
                checkState(instance.getInstanceType() == CanonicalInstanceReference.InstanceType.PENDING_DATA_REF, "A pending data position should be found, critical protocol exceptions");
                CanonicalInstanceReference filledDataRef = CanonicalProtocolSerialFactory.createFilledDataSerializedReference(position, instanceReference.getDataObjectSerialization());
                instanceReferenceList.set(position, filledDataRef);
                instanceObjectList.set(position, CanonicalProtocolSerialFactory.deserialize(instanceReference.getDataObjectSerialization()));
                break;
            case SERVER_DATA_OBJECT:
                currentInstanceNumber = getNextInstanceNumber();
                checkState(instanceReference.getInstanceNr() == currentInstanceNumber, "exceptions on merge of %s. currentInstanceNumber = %s", instanceReference, currentInstanceNumber);
                CanonicalInstanceReference serverDataRef = CanonicalProtocolSerialFactory.createServerDataInstanceReference(currentInstanceNumber, instanceReference.getDataObjectSerialization());
                addStackReferenceInstance(serverDataRef, CanonicalProtocolSerialFactory.deserialize(instanceReference.getDataObjectSerialization()), currentInstanceNumber);
                break;
        }
    }


}
