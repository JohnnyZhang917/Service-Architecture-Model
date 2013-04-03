package eu.pmsoft.sam.protocol.transport.data;

import com.dyuproject.protostuff.ByteString;
import com.google.inject.Key;
import eu.pmsoft.sam.protocol.transport.BindingKey;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.CanonicalMethodCall;

import java.io.*;
import java.lang.reflect.Method;

public class CanonicalProtocolSerialFactory {


    public static <T> CanonicalInstanceReference createBindingKeyReference(int serviceInstanceNr, BindingKey key) {
        CanonicalInstanceReference bindingReference = new CanonicalInstanceReference();
        bindingReference.setInstanceType(CanonicalInstanceReference.InstanceType.BINDING_KEY);
        bindingReference.setInstanceNr(serviceInstanceNr);
        bindingReference.setKey(key);
        return bindingReference;
    }

    public static <T> CanonicalInstanceReference createBindingKeyReference(int serviceInstanceNr, Key<T> key) {
        return createBindingKeyReference(serviceInstanceNr, SerializableKey.fromKeySerializable(key));
    }

    public static <T> CanonicalInstanceReference createExternalInstanceReference(int serviceInstanceNr, Key<T> key) {
        CanonicalInstanceReference externalInstance = new CanonicalInstanceReference();
        externalInstance.setInstanceType(CanonicalInstanceReference.InstanceType.EXTERNAL_INSTANCE_REFERENCE);
        externalInstance.setKey(SerializableKey.fromKeySerializable(key));
        externalInstance.setInstanceNr(serviceInstanceNr);
        return externalInstance;
    }

    public static CanonicalInstanceReference createPendingDataInstanceReference(int serviceInstanceNr, Class<?> returnType) {
        return createPendingDataInstanceReference(serviceInstanceNr, returnType.toString());
    }

    public static CanonicalInstanceReference createPendingDataInstanceReference(int serviceInstanceNr, String returnType) {
        CanonicalInstanceReference instance = new CanonicalInstanceReference();
        instance.setInstanceType(CanonicalInstanceReference.InstanceType.PENDING_DATA_REF);
        instance.setInstanceNr(serviceInstanceNr);
        instance.setDataType(returnType);
        return instance;
    }

    public static CanonicalInstanceReference createFilledDataInstanceReference(int returnInstance, Object returnObject) {
        CanonicalInstanceReference filledData = new CanonicalInstanceReference();
        filledData.setInstanceType(CanonicalInstanceReference.InstanceType.FILLED_DATA_INSTANCE);
        filledData.setInstanceNr(returnInstance);
        filledData.setDataObjectSerialization(serialize((Serializable) returnObject));
        return filledData;
    }

    public static CanonicalInstanceReference createFilledDataSerializedReference(int returnInstance, ByteString returnObject) {
        CanonicalInstanceReference filledData = new CanonicalInstanceReference();
        filledData.setInstanceType(CanonicalInstanceReference.InstanceType.FILLED_DATA_INSTANCE);
        filledData.setInstanceNr(returnInstance);
        filledData.setDataObjectSerialization(returnObject);
        return filledData;
    }

    public static CanonicalInstanceReference createClientDataObject(int serviceInstanceNr, ByteString arg) {
        CanonicalInstanceReference dataInstance = new CanonicalInstanceReference();
        dataInstance.setInstanceType(CanonicalInstanceReference.InstanceType.CLIENT_DATA_OBJECT);
        dataInstance.setInstanceNr(serviceInstanceNr);
        dataInstance.setDataObjectSerialization(arg);
        return dataInstance;
    }

    public static CanonicalInstanceReference createClientDataObjectSerializable(int serviceInstanceNr, Object arg) {
        CanonicalInstanceReference dataInstance = new CanonicalInstanceReference();
        dataInstance.setInstanceType(CanonicalInstanceReference.InstanceType.CLIENT_DATA_OBJECT);
        dataInstance.setInstanceNr(serviceInstanceNr);
        //TODO recorder must know how to serialize parameters, transfer this logic there after architecture precompilation
        dataInstance.setDataObjectSerialization(serialize((Serializable) arg));
        return dataInstance;
    }

    public static CanonicalInstanceReference createServerBindingKeyInstanceReference(int serviceInstanceNr, Key<?> key) {
        return createServerBindingKeyInstanceReference(serviceInstanceNr, SerializableKey.fromKeySerializable(key));
    }

    public static CanonicalInstanceReference createServerBindingKeyInstanceReference(int serviceInstanceNr, BindingKey key) {
        CanonicalInstanceReference bindingReference = new CanonicalInstanceReference();
        bindingReference.setInstanceType(CanonicalInstanceReference.InstanceType.SERVER_BINDING_KEY);
        bindingReference.setInstanceNr(serviceInstanceNr);
        bindingReference.setKey(key);
        return bindingReference;
    }

    public static CanonicalInstanceReference createServerPendingDataInstanceReference(int serviceInstanceNr, Class<?> returnType) {
        return createServerPendingDataInstanceReference(serviceInstanceNr, returnType.toString());
    }

    public static CanonicalInstanceReference createServerPendingDataInstanceReference(int serviceInstanceNr, String dataType) {
        CanonicalInstanceReference bindingReference = new CanonicalInstanceReference();
        bindingReference.setInstanceType(CanonicalInstanceReference.InstanceType.SERVER_PENDING_DATA_REF);
        bindingReference.setInstanceNr(serviceInstanceNr);
        bindingReference.setDataType(dataType);
        return bindingReference;

    }

    public static CanonicalInstanceReference createServerDataInstanceReference(int serviceInstanceNr, ByteString dataObjectSerialization) {
        CanonicalInstanceReference bindingReference = new CanonicalInstanceReference();
        bindingReference.setInstanceType(CanonicalInstanceReference.InstanceType.SERVER_DATA_OBJECT);
        bindingReference.setInstanceNr(serviceInstanceNr);
        bindingReference.setDataObjectSerialization(dataObjectSerialization);
        return bindingReference;

    }

    public static CanonicalInstanceReference createServerDataInstanceReference(int serviceInstanceNr, Object dataObjectSerialization) {
        return createServerDataInstanceReference(serviceInstanceNr, serialize((Serializable) dataObjectSerialization));
    }

    public static Object deserialize(ByteString dataObjectSerialization) {
        ByteArrayInputStream bis = new ByteArrayInputStream(dataObjectSerialization.toByteArray());
        ObjectInput in = null;
        try {
            try {
                in = new ObjectInputStream(bis);
                return in.readObject();
            } finally {
                bis.close();
                in.close();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("TODO", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("TODO", e);
        }
    }

    public static ByteString serialize(Serializable object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(object);
                return ByteString.copyFrom(bos.toByteArray());
            } finally {
                out.close();
                bos.close();
            }
        } catch (IOException e) {
            //TODO
            throw new RuntimeException("TODO", e);
        }
    }

    public static CanonicalMethodCall createMethodCall(int serviceSlotNr, int serviceInstanceNr, int returnInstanceNr, Method method, int[] arguments) {
        //TODO optional fields

        CanonicalMethodCall call = new CanonicalMethodCall();
        call.setInstanceNr(serviceInstanceNr)
                .setMethodSignature(method.hashCode())
                .setSlotNr(serviceSlotNr)
                .setReturnInstanceId(returnInstanceNr);
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                // TODO faster creation without for and Integer boxing
                int arg = arguments[i];
                call.addArgumentsReference(arg);
            }
        }
        return call;
    }

}
