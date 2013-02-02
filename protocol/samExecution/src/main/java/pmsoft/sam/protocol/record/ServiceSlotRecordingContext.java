package pmsoft.sam.protocol.record;

import com.google.inject.Key;

import java.lang.reflect.Method;

interface ServiceSlotRecordingContext {

	<T> void recordExternalMethodCall(Key<T> key, int serviceInstanceNr, Method method, Object[] args);

	<T, S> CanonicalInstanceRecorder<S> recordExternalMethodCallWithInterfaceReturnType(Key<T> key, int serviceInstanceNr,
			Method method, Object[] args, Class<S> returnType);

	<T, S> S recordExternalMethodCallWithReturnType(Key<T> key, int serviceInstanceNr, Method method, Object[] args,
			Class<S> returnType);
	
}
