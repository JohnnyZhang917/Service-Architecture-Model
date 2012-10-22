package pmsoft.sam.protocol.injection.internal;

import java.lang.reflect.Method;

import com.google.inject.Key;

import pmsoft.sam.protocol.execution.CanonicalProtocolRequestHandler;
import pmsoft.sam.protocol.execution.model.MethodCall;

interface MethodRecordContext extends CanonicalProtocolRequestHandler {

	void pushMethodCall(MethodCall call);

	Object flushMethodCall(MethodCall call);

	void flushExecution();

	/**
	 * In the case that a reference to a instance from Slot "X!=targetSlot" is
	 * passed as argument to method execution for reference of slot
	 * "targetSlot", then the methodRecordContext creates a mapping between this
	 * instances creating on slot "targetSlot" a ExternalInstance reference.
	 * 
	 * The InstanceRegistry objects don't have access to this mapping.
	 * 
	 * @param targetSlot
	 *            The slot where a external instance is passed as argument
	 * @param externalSlotRecordReference
	 *            The recording object of the argument instance
	 * @return the instance number assigned in InstanceRegistry from slot
	 *         "targetSlot"
	 */
	int createExternalInstanceBinding(int targetSlot, CanonicalInstanceRecorder<?> externalSlotRecordReference);

	
	
	<T> void recordExternalMethodCall(int serviceSlotNr,Key<T> key, int serviceInstanceNr, Method method, Object[] args);

	<T, S> CanonicalInstanceRecorder<S> recordExternalMethodCallWithInterfaceReturnType(int serviceSlotNr,Key<T> key, int serviceInstanceNr,
			Method method, Object[] args, Class<S> returnType);

	<T, S> S recordExternalMethodCallWithReturnType(int serviceSlotNr,Key<T> key, int serviceInstanceNr, Method method, Object[] args,
			Class<S> returnType);
	
	
}
